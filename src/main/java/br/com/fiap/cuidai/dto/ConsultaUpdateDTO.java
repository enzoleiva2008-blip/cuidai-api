package br.com.fiap.cuidai.dto;

import br.com.fiap.cuidai.model.StatusConsulta;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

/**
 * Atualizacao de uma consulta (PUT /consultas/{id}): remarcar, trocar medico
 * ou registrar o resultado (REALIZADA com tempo de espera, FALTOU, CANCELADA).
 */
public class ConsultaUpdateDTO {

    @NotNull(message = "Informe o medico (medicoId)")
    @Positive(message = "O medicoId deve ser positivo")
    private Long medicoId;

    @NotNull(message = "Informe a especialidade (especialidadeId)")
    @Positive(message = "O especialidadeId deve ser positivo")
    private Long especialidadeId;

    @NotNull(message = "Informe a data e hora da consulta")
    private LocalDateTime dataConsulta;

    @NotNull(message = "Informe o status: AGENDADA, REALIZADA, FALTOU ou CANCELADA")
    private StatusConsulta status;

    @PositiveOrZero(message = "O tempo de espera nao pode ser negativo")
    @Max(value = 9999, message = "O tempo de espera deve ter no maximo 9999 minutos")
    private Integer esperaMin;

    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }

    public Long getEspecialidadeId() { return especialidadeId; }
    public void setEspecialidadeId(Long especialidadeId) { this.especialidadeId = especialidadeId; }

    public LocalDateTime getDataConsulta() { return dataConsulta; }
    public void setDataConsulta(LocalDateTime dataConsulta) { this.dataConsulta = dataConsulta; }

    public StatusConsulta getStatus() { return status; }
    public void setStatus(StatusConsulta status) { this.status = status; }

    public Integer getEsperaMin() { return esperaMin; }
    public void setEsperaMin(Integer esperaMin) { this.esperaMin = esperaMin; }
}
