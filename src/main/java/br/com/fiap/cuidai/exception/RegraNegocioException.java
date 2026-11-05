package br.com.fiap.cuidai.exception;

/** Regra de negocio violada. O handler devolve 409 Conflict. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
