package br.com.fiap.cuidai.exception;

/** Valor que deveria ser unico ja existe (CPF, Cartao SUS, CRM). Devolve 409. */
public class DadoDuplicadoException extends RegraNegocioException {

    public DadoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
