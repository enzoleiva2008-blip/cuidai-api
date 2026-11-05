package br.com.fiap.cuidai.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * Agendamento pela rota aninhada POST /cidadaos/{id}/consultas.
 * O cidadao vem da URL, por isso nao aparece aqui.
 */
public class AgendamentoRequestDTO {

    @NotNull(message = "Informe o medico (medicoId)")
    @Positive(message = "O medicoId deve ser positivo")
    private Long medicoId;

    @NotNull(message = "Informe a especialidade (especialidadeId)")
    @Positive(message = "O especialidadeId deve ser positivo")
    private Long especialidadeId;

    @NotNull(message = "Informe a data e hora da consulta")
    @Future(message = "A consulta deve ser marcada para uma data futura")
    private LocalDateTime dataConsulta;

    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }

    public Long getEspecialidadeId() { return especialidadeId; }
    public void setEspecialidadeId(Long especialidadeId) { this.especialidadeId = especialidadeId; }

    public LocalDateTime getDataConsulta() { return dataConsulta; }
    public void setDataConsulta(LocalDateTime dataConsulta) { this.dataConsulta = dataConsulta; }
}
