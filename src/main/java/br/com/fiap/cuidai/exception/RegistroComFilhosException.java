package br.com.fiap.cuidai.exception;

/** Tentativa de remover um registro que ainda tem registros ligados a ele. Devolve 409. */
public class RegistroComFilhosException extends RegraNegocioException {

    public RegistroComFilhosException(String mensagem) {
        super(mensagem);
    }
}
