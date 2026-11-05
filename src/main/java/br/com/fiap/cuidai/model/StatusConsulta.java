package br.com.fiap.cuidai.model;

/** Situacoes possiveis de uma consulta (mesmos valores do CHECK no banco). */
public enum StatusConsulta {
    AGENDADA,
    REALIZADA,
    FALTOU,
    CANCELADA
}
