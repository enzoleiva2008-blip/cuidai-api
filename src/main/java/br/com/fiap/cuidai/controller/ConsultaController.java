package br.com.fiap.cuidai.controller;

import br.com.fiap.cuidai.dao.CidadaoDAO;
import br.com.fiap.cuidai.dao.ConsultaDAO;
import br.com.fiap.cuidai.dao.MedicoDAO;
import br.com.fiap.cuidai.dto.AgendamentoRequestDTO;
import br.com.fiap.cuidai.dto.ConsultaRequestDTO;
import br.com.fiap.cuidai.dto.ConsultaResponseDTO;
import br.com.fiap.cuidai.dto.ConsultaUpdateDTO;
import br.com.fiap.cuidai.exception.HorarioOcupadoException;
import br.com.fiap.cuidai.exception.RecursoNaoEncontradoException;
import br.com.fiap.cuidai.exception.RegraNegocioException;
import br.com.fiap.cuidai.model.Cidadao;
import br.com.fiap.cuidai.model.Consulta;
import br.com.fiap.cuidai.model.Medico;
import br.com.fiap.cuidai.model.StatusConsulta;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Rotas REST de consultas, incluindo as rotas aninhadas do relacionamento 1:N
 * (Cidadao 1 -> N Consultas): GET e POST em /cidadaos/{id}/consultas.
 *
 * Regras de negocio aplicadas no agendamento:
 *  - o medico precisa estar ativo;
 *  - o medico precisa atender a especialidade escolhida (tabela N:N);
 *  - o medico nao pode ter outra consulta AGENDADA no mesmo horario (HorarioOcupadoException);
 *  - a prioridade na fila e calculada pelo dominio (Cidadao.getPrioridade()).
 */
@RestController
public class ConsultaController {

    private final ConsultaDAO consultaDAO;
    private final CidadaoDAO cidadaoDAO;
    private final MedicoDAO medicoDAO;
    private final ModelMapper mapper;

    public ConsultaController(ConsultaDAO consultaDAO, CidadaoDAO cidadaoDAO,
                              MedicoDAO medicoDAO, ModelMapper mapper) {
        this.consultaDAO = consultaDAO;
        this.cidadaoDAO = cidadaoDAO;
        this.medicoDAO = medicoDAO;
        this.mapper = mapper;
    }

    // ================================================================ CRUD /consultas

    /** POST /consultas -> 201 + Location | 400 | 404 | 409 */
    @PostMapping("/consultas")
    public ResponseEntity<ConsultaResponseDTO> cadastrar(@Valid @RequestBody ConsultaRequestDTO dto) {
        Consulta consulta = agendar(dto.getCidadaoId(), dto);
        return criado(consulta);
    }

    /** GET /consultas -> 200 */
    @GetMapping("/consultas")
    public List<ConsultaResponseDTO> listar() {
        return consultaDAO.listar().stream().map(this::paraResposta).toList();
    }

    /** GET /consultas/{id} -> 200 | 404 */
    @GetMapping("/consultas/{id}")
    public ConsultaResponseDTO buscar(@PathVariable Long id) {
        return paraResposta(buscarOuFalhar(id));
    }

    /** PUT /consultas/{id} -> 200 | 400 | 404 | 409 (consulta ja finalizada, horario ocupado...) */
    @PutMapping("/consultas/{id}")
    public ConsultaResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody ConsultaUpdateDTO dto) {
        Consulta consulta = buscarOuFalhar(id);
        consulta.validarSePodeSerAlterada();

        boolean mudouAgenda = !Objects.equals(consulta.getMedicoId(), dto.getMedicoId())
                || !Objects.equals(consulta.getEspecialidadeId(), dto.getEspecialidadeId())
                || !Objects.equals(consulta.getDataConsulta(), dto.getDataConsulta());
        if (mudouAgenda) {
            if (dto.getDataConsulta().isBefore(LocalDateTime.now())) {
                throw new RegraNegocioException("Para remarcar, escolha uma data futura.");
            }
            validarAgenda(dto.getMedicoId(), dto.getEspecialidadeId(), dto.getDataConsulta(), id);
        }

        if (dto.getStatus() == StatusConsulta.REALIZADA && dto.getEsperaMin() == null) {
            throw new RegraNegocioException("Informe o tempo de espera (esperaMin) ao registrar a consulta como REALIZADA.");
        }

        consulta.setMedicoId(dto.getMedicoId());
        consulta.setEspecialidadeId(dto.getEspecialidadeId());
        consulta.setDataConsulta(dto.getDataConsulta());
        consulta.setStatus(dto.getStatus());
        // So faz sentido guardar tempo de espera se o cidadao foi atendido
        consulta.setEsperaMin(dto.getStatus() == StatusConsulta.REALIZADA ? dto.getEsperaMin() : null);

        consultaDAO.atualizar(consulta);
        return paraResposta(buscarOuFalhar(id));
    }

    /** DELETE /consultas/{id} -> 204 | 404 | 409 (consulta com lembretes) */
    @DeleteMapping("/consultas/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (!consultaDAO.remover(id)) {
            throw new RecursoNaoEncontradoException("Consulta " + id + " nao encontrada.");
        }
        return ResponseEntity.noContent().build();
    }

    // ================================================================ rotas aninhadas 1:N

    /** GET /cidadaos/{id}/consultas -> 200 | 404 */
    @GetMapping("/cidadaos/{cidadaoId}/consultas")
    public List<ConsultaResponseDTO> listarDoCidadao(@PathVariable Long cidadaoId) {
        buscarCidadaoOuFalhar(cidadaoId);
        return consultaDAO.listarPorCidadao(cidadaoId).stream().map(this::paraResposta).toList();
    }

    /** POST /cidadaos/{id}/consultas -> 201 + Location | 400 | 404 | 409 */
    @PostMapping("/cidadaos/{cidadaoId}/consultas")
    public ResponseEntity<ConsultaResponseDTO> agendarParaCidadao(@PathVariable Long cidadaoId,
                                                                  @Valid @RequestBody AgendamentoRequestDTO dto) {
        Consulta consulta = agendar(cidadaoId, dto);
        return criado(consulta);
    }

    // ================================================================ regras de negocio

    /** Fluxo de agendamento usado pelas duas rotas de cadastro. */
    private Consulta agendar(Long cidadaoId, AgendamentoRequestDTO dto) {
        Cidadao cidadao = buscarCidadaoOuFalhar(cidadaoId);
        validarAgenda(dto.getMedicoId(), dto.getEspecialidadeId(), dto.getDataConsulta(), null);

        Consulta consulta = mapper.map(dto, Consulta.class);
        consulta.setCidadaoId(cidadaoId);
        consulta.setDataAgendamento(LocalDateTime.now());
        consulta.setStatus(StatusConsulta.AGENDADA);
        consulta.setPrioridade(cidadao.getPrioridade());
        consulta.setEsperaMin(null);

        Consulta salva = consultaDAO.inserir(consulta);
        return buscarOuFalhar(salva.getId()); // recarrega com os nomes (JOIN)
    }

    private void validarAgenda(Long medicoId, Long especialidadeId, LocalDateTime dataConsulta, Long ignorarId) {
        Medico medico = medicoDAO.buscarPorId(medicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medico " + medicoId + " nao encontrado."));

        if (!medico.isAtivo()) {
            throw new RegraNegocioException("O medico " + medico.getNome() + " nao esta recebendo agendamentos.");
        }
        if (!medicoDAO.atendeEspecialidade(medicoId, especialidadeId)) {
            throw new RegraNegocioException("O medico " + medico.getNome() + " nao atende a especialidade "
                    + especialidadeId + ".");
        }
        if (consultaDAO.existeConsultaNoHorario(medicoId, dataConsulta, ignorarId)) {
            throw new HorarioOcupadoException(medicoId, dataConsulta);
        }
    }

    // ================================================================ auxiliares

    private Consulta buscarOuFalhar(Long id) {
        return consultaDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta " + id + " nao encontrada."));
    }

    private Cidadao buscarCidadaoOuFalhar(Long cidadaoId) {
        return cidadaoDAO.buscarPorId(cidadaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cidadao " + cidadaoId + " nao encontrado."));
    }

    /** 201 Created com Location apontando para /consultas/{id} */
    private ResponseEntity<ConsultaResponseDTO> criado(Consulta consulta) {
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/consultas/{id}").buildAndExpand(consulta.getId()).toUri();
        return ResponseEntity.created(location).body(paraResposta(consulta));
    }

    private ConsultaResponseDTO paraResposta(Consulta consulta) {
        return mapper.map(consulta, ConsultaResponseDTO.class);
    }
}
