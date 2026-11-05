package br.com.fiap.cuidai.dto;

import br.com.fiap.cuidai.model.StatusConsulta;

import java.time.LocalDateTime;

/** O que a API devolve sobre uma consulta, ja com os nomes para o front exibir. */
public class ConsultaResponseDTO {

    private Long id;
    private Long cidadaoId;
    private String nomeCidadao;
    private Long medicoId;
    private String nomeMedico;
    private Long especialidadeId;
    private String nomeEspecialidade;
    private LocalDateTime dataAgendamento;
    private LocalDateTime dataConsulta;
    private StatusConsulta status;
    private Integer prioridade;
    private Integer esperaMin;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCidadaoId() { return cidadaoId; }
    public void setCidadaoId(Long cidadaoId) { this.cidadaoId = cidadaoId; }

    public String getNomeCidadao() { return nomeCidadao; }
    public void setNomeCidadao(String nomeCidadao) { this.nomeCidadao = nomeCidadao; }

    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }

    public String getNomeMedico() { return nomeMedico; }
    public void setNomeMedico(String nomeMedico) { this.nomeMedico = nomeMedico; }

    public Long getEspecialidadeId() { return especialidadeId; }
    public void setEspecialidadeId(Long especialidadeId) { this.especialidadeId = especialidadeId; }

    public String getNomeEspecialidade() { return nomeEspecialidade; }
    public void setNomeEspecialidade(String nomeEspecialidade) { this.nomeEspecialidade = nomeEspecialidade; }

    public LocalDateTime getDataAgendamento() { return dataAgendamento; }
    public void setDataAgendamento(LocalDateTime dataAgendamento) { this.dataAgendamento = dataAgendamento; }

    public LocalDateTime getDataConsulta() { return dataConsulta; }
    public void setDataConsulta(LocalDateTime dataConsulta) { this.dataConsulta = dataConsulta; }

    public StatusConsulta getStatus() { return status; }
    public void setStatus(StatusConsulta status) { this.status = status; }

    public Integer getPrioridade() { return prioridade; }
    public void setPrioridade(Integer prioridade) { this.prioridade = prioridade; }

    public Integer getEsperaMin() { return esperaMin; }
    public void setEsperaMin(Integer esperaMin) { this.esperaMin = esperaMin; }
}
