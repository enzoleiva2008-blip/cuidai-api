package br.com.fiap.cuidai.exception;

/**
 * Erro inesperado ao falar com o banco. O handler devolve 500 com mensagem generica:
 * o detalhe do SQL fica so no log do servidor, nunca vai para o cliente.
 */
public class AcessoBancoException extends RuntimeException {

    public AcessoBancoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
