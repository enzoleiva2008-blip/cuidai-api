package br.com.fiap.cuidai.exception;

/** O id pedido nao existe no banco. O handler devolve 404 Not Found. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
