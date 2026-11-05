package br.com.fiap.cuidai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Dados para cadastrar ou atualizar um medico. */
public class MedicoRequestDTO {

    @NotNull(message = "Informe a UBS do medico (ubsId)")
    @Positive(message = "O ubsId deve ser positivo")
    private Long ubsId;

    @NotBlank(message = "O nome e obrigatorio")
    @Size(max = 80, message = "O nome deve ter no maximo 80 caracteres")
    private String nome;

    @NotBlank(message = "O CRM e obrigatorio")
    @Size(max = 15, message = "O CRM deve ter no maximo 15 caracteres")
    private String crm;

    @NotNull(message = "Informe se o medico esta ativo (true/false)")
    private Boolean ativo;

    public Long getUbsId() { return ubsId; }
    public void setUbsId(Long ubsId) { this.ubsId = ubsId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
