package br.com.fiap.cuidai.handler;

import br.com.fiap.cuidai.dto.ErroResponseDTO;
import br.com.fiap.cuidai.exception.RecursoNaoEncontradoException;
import br.com.fiap.cuidai.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Handler global: transforma TODA excecao em um JSON padronizado (ErroResponseDTO),
 * com o status HTTP certo e sem stack trace ou SQL.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 400 - Bean Validation falhou (@NotBlank, @Size, @Future...) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> validacao(MethodArgumentNotValidException e, HttpServletRequest req) {
        List<String> campos = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "Dados invalidos. Confira os campos.", req, campos);
    }

    /** 400 - JSON malformado ou valor no formato errado (data, enum de status...) */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> jsonInvalido(HttpMessageNotReadableException e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST,
                "JSON invalido ou campo com formato errado. Datas: 2026-11-12T08:00:00. "
                        + "Status: AGENDADA, REALIZADA, FALTOU ou CANCELADA.", req, null);
    }

    /** 400 - id na URL que nao e numero (ex.: /cidadaos/abc) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> parametroInvalido(MethodArgumentTypeMismatchException e,
                                                             HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Parametro '" + e.getName() + "' invalido.", req, null);
    }

    /** 404 - id nao existe */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> naoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage(), req, null);
    }

    /** 404 - rota que nao existe */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponseDTO> rotaInexistente(NoResourceFoundException e, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "Rota nao encontrada.", req, null);
    }

    /** 405 - metodo HTTP nao suportado na rota (ex.: DELETE /cidadaos) */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> metodoNaoSuportado(HttpRequestMethodNotSupportedException e,
                                                              HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED,
                "Metodo " + e.getMethod() + " nao e suportado nesta rota.", req, null);
    }

    /** 415 - corpo enviado sem Content-Type: application/json */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tipoNaoSuportado(HttpMediaTypeNotSupportedException e,
                                                            HttpServletRequest req) {
        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie o corpo como application/json.", req, null);
    }

    /** 409 - regra de negocio violada, dado duplicado ou remocao de registro com filhos */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDTO> regraNegocio(RegraNegocioException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, e.getMessage(), req, null);
    }

    /** 500 - qualquer outro erro. O detalhe vai so para o log, nunca para o cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> erroInesperado(Exception e, HttpServletRequest req) {
        LOG.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno. Tente novamente em alguns instantes.", req, null);
    }

    private ResponseEntity<ErroResponseDTO> resposta(HttpStatus status, String mensagem,
                                                     HttpServletRequest req, List<String> campos) {
        ErroResponseDTO corpo = new ErroResponseDTO(status.value(), status.getReasonPhrase(),
                mensagem, req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
