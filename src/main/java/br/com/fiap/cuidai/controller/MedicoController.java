package br.com.fiap.cuidai.controller;

import br.com.fiap.cuidai.dao.MedicoDAO;
import br.com.fiap.cuidai.dto.MedicoRequestDTO;
import br.com.fiap.cuidai.dto.MedicoResponseDTO;
import br.com.fiap.cuidai.exception.RecursoNaoEncontradoException;
import br.com.fiap.cuidai.model.Medico;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** Rotas REST de medicos. */
@RestController
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoDAO medicoDAO;
    private final ModelMapper mapper;

    public MedicoController(MedicoDAO medicoDAO, ModelMapper mapper) {
        this.medicoDAO = medicoDAO;
        this.mapper = mapper;
    }

    /** POST /medicos -> 201 + Location | 400 | 404 (UBS nao existe) | 409 (CRM repetido) */
    @PostMapping
    public ResponseEntity<MedicoResponseDTO> cadastrar(@Valid @RequestBody MedicoRequestDTO dto) {
        Medico medico = medicoDAO.inserir(mapper.map(dto, Medico.class));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(medico.getId()).toUri();
        return ResponseEntity.created(location).body(paraResposta(medico));
    }

    /** GET /medicos -> 200 */
    @GetMapping
    public List<MedicoResponseDTO> listar() {
        return medicoDAO.listar().stream().map(this::paraResposta).toList();
    }

    /** GET /medicos/{id} -> 200 | 404 */
    @GetMapping("/{id}")
    public MedicoResponseDTO buscar(@PathVariable Long id) {
        return paraResposta(medicoDAO.buscarPorId(id).orElseThrow(() -> naoEncontrado(id)));
    }

    /** PUT /medicos/{id} -> 200 | 400 | 404 | 409 */
    @PutMapping("/{id}")
    public MedicoResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody MedicoRequestDTO dto) {
        Medico medico = mapper.map(dto, Medico.class);
        medico.setId(id);
        if (!medicoDAO.atualizar(medico)) {
            throw naoEncontrado(id);
        }
        return paraResposta(medico);
    }

    /** DELETE /medicos/{id} -> 204 | 404 | 409 (medico com consultas ou especialidades) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (!medicoDAO.remover(id)) {
            throw naoEncontrado(id);
        }
        return ResponseEntity.noContent().build();
    }

    private RecursoNaoEncontradoException naoEncontrado(Long id) {
        return new RecursoNaoEncontradoException("Medico " + id + " nao encontrado.");
    }

    private MedicoResponseDTO paraResposta(Medico medico) {
        return mapper.map(medico, MedicoResponseDTO.class);
    }
}
