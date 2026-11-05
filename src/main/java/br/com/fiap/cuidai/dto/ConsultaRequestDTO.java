package br.com.fiap.cuidai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Agendamento pela rota POST /consultas: igual ao agendamento, mais o cidadao. */
public class ConsultaRequestDTO extends AgendamentoRequestDTO {

    @NotNull(message = "Informe o cidadao (cidadaoId)")
    @Positive(message = "O cidadaoId deve ser positivo")
    private Long cidadaoId;

    public Long getCidadaoId() { return cidadaoId; }
    public void setCidadaoId(Long cidadaoId) { this.cidadaoId = cidadaoId; }
}
