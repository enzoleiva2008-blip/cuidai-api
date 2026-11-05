package br.com.fiap.cuidai.model;

/**
 * Medico que atende em uma UBS. Tabela: TB_MEDICO.
 * Um medico inativo nao recebe novos agendamentos.
 */
public class Medico {

    private Long id;
    private Long ubsId;
    private String nome;
    private String crm;
    private boolean ativo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUbsId() { return ubsId; }
    public void setUbsId(Long ubsId) { this.ubsId = ubsId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
