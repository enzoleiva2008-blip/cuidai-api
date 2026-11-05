package br.com.fiap.cuidai.controller;

import br.com.fiap.cuidai.dao.CidadaoDAO;
import br.com.fiap.cuidai.dto.CidadaoRequestDTO;
import br.com.fiap.cuidai.dto.CidadaoResponseDTO;
import br.com.fiap.cuidai.exception.RecursoNaoEncontradoException;
import br.com.fiap.cuidai.model.Cidadao;
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

/** Rotas REST de cidadaos. As rotas aninhadas /cidadaos/{id}/consultas estao no ConsultaController. */
@RestController
@RequestMapping("/cidadaos")
public class CidadaoController {

    private final CidadaoDAO cidadaoDAO;
    private final ModelMapper mapper;

    public CidadaoController(CidadaoDAO cidadaoDAO, ModelMapper mapper) {
        this.cidadaoDAO = cidadaoDAO;
        this.mapper = mapper;
    }

    /** POST /cidadaos -> 201 + Location | 400 | 409 (CPF ou Cartao SUS repetido) */
    @PostMapping
    public ResponseEntity<CidadaoResponseDTO> cadastrar(@Valid @RequestBody CidadaoRequestDTO dto) {
        Cidadao cidadao = cidadaoDAO.inserir(mapper.map(dto, Cidadao.class));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(cidadao.getId()).toUri();
        return ResponseEntity.created(location).body(paraResposta(cidadao));
    }

    /** GET /cidadaos -> 200 */
    @GetMapping
    public List<CidadaoResponseDTO> listar() {
        return cidadaoDAO.listar().stream().map(this::paraResposta).toList();
    }

    /** GET /cidadaos/{id} -> 200 | 404 */
    @GetMapping("/{id}")
    public CidadaoResponseDTO buscar(@PathVariable Long id) {
        return paraResposta(buscarOuFalhar(id));
    }

    /** PUT /cidadaos/{id} -> 200 | 400 | 404 | 409 */
    @PutMapping("/{id}")
    public CidadaoResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody CidadaoRequestDTO dto) {
        Cidadao cidadao = mapper.map(dto, Cidadao.class);
        cidadao.setId(id);
        if (!cidadaoDAO.atualizar(cidadao)) {
            throw naoEncontrado(id);
        }
        return paraResposta(cidadao);
    }

    /** DELETE /cidadaos/{id} -> 204 | 404 | 409 (cidadao com consultas) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (!cidadaoDAO.remover(id)) {
            throw naoEncontrado(id);
        }
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- auxiliares

    private Cidadao buscarOuFalhar(Long id) {
        return cidadaoDAO.buscarPorId(id).orElseThrow(() -> naoEncontrado(id));
    }

    private RecursoNaoEncontradoException naoEncontrado(Long id) {
        return new RecursoNaoEncontradoException("Cidadao " + id + " nao encontrado.");
    }

    private CidadaoResponseDTO paraResposta(Cidadao cidadao) {
        return mapper.map(cidadao, CidadaoResponseDTO.class);
    }
}
