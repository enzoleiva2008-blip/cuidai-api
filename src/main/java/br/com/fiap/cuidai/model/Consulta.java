package br.com.fiap.cuidai.model;

import br.com.fiap.cuidai.exception.RegraNegocioException;

import java.time.LocalDateTime;

/**
 * Consulta agendada por um cidadao. Tabela: TB_CONSULTA.
 * Relacionamento 1:N -> um Cidadao tem varias Consultas.
 */
public class Consulta {

    private Long id;
    private Long cidadaoId;
    private Long medicoId;
    private Long especialidadeId;
    private LocalDateTime dataAgendamento;
    private LocalDateTime dataConsulta;
    private StatusConsulta status;
    private Integer prioridade;
    private Integer esperaMin;

    // Preenchidos apenas na leitura (JOIN), para o front mostrar nomes em vez de codigos
    private String nomeCidadao;
    private String nomeMedico;
    private String nomeEspecialidade;

    /**
     * Regra do dominio: so uma consulta AGENDADA pode ser alterada.
     * Depois de realizada, cancelada ou com falta registrada, ela vira historico
     * (e esse historico alimenta a IA do CuidAi).
     */
    public void validarSePodeSerAlterada() {
        if (status != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException(
                    "A consulta " + id + " esta com status " + status + " e nao pode mais ser alterada.");
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCidadaoId() { return cidadaoId; }
    public void setCidadaoId(Long cidadaoId) { this.cidadaoId = cidadaoId; }

    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }

    public Long getEspecialidadeId() { return especialidadeId; }
    public void setEspecialidadeId(Long especialidadeId) { this.especialidadeId = especialidadeId; }

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

    public String getNomeCidadao() { return nomeCidadao; }
    public void setNomeCidadao(String nomeCidadao) { this.nomeCidadao = nomeCidadao; }

    public String getNomeMedico() { return nomeMedico; }
    public void setNomeMedico(String nomeMedico) { this.nomeMedico = nomeMedico; }

    public String getNomeEspecialidade() { return nomeEspecialidade; }
    public void setNomeEspecialidade(String nomeEspecialidade) { this.nomeEspecialidade = nomeEspecialidade; }
}
