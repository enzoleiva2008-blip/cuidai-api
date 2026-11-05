package br.com.fiap.cuidai.dao;

import br.com.fiap.cuidai.exception.AcessoBancoException;
import br.com.fiap.cuidai.exception.DadoDuplicadoException;
import br.com.fiap.cuidai.exception.RecursoNaoEncontradoException;
import br.com.fiap.cuidai.exception.RegistroComFilhosException;

import java.sql.SQLException;

/**
 * Traduz os erros do Oracle para as excecoes do dominio.
 * Assim o controller e o handler nunca precisam conhecer codigos ORA-.
 */
final class ErroBanco {

    private static final int ORA_UNIQUE = 1;          // ORA-00001: valor duplicado em UNIQUE/PK
    private static final int ORA_PAI_NAO_EXISTE = 2291; // ORA-02291: FK aponta para registro inexistente
    private static final int ORA_TEM_FILHOS = 2292;     // ORA-02292: registro tem filhos ligados a ele

    private ErroBanco() {
    }

    static RuntimeException traduzir(SQLException e, String entidade) {
        switch (e.getErrorCode()) {
            case ORA_UNIQUE:
                return new DadoDuplicadoException(
                        "Ja existe " + entidade + " com esses dados (CPF, Cartao SUS ou CRM repetido).");
            case ORA_PAI_NAO_EXISTE:
                return new RecursoNaoEncontradoException(
                        "Um dos codigos informados para " + entidade + " nao existe no sistema.");
            case ORA_TEM_FILHOS:
                return new RegistroComFilhosException(
                        "Nao e possivel remover " + entidade + ": existem registros ligados a esse cadastro.");
            default:
                return new AcessoBancoException("Erro ao acessar o banco (" + entidade + ")", e);
        }
    }
}
