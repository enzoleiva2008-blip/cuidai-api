package br.com.fiap.cuidai.dto;

/** O que a API devolve sobre um medico. */
public class MedicoResponseDTO {

    private Long id;
    private Long ubsId;
    private String nome;
    private String crm;
    private Boolean ativo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUbsId() { return ubsId; }
    public void setUbsId(Long ubsId) { this.ubsId = ubsId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
