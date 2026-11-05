package br.com.fiap.cuidai.dto;

import java.time.LocalDate;

/** O que a API devolve sobre um cidadao. Inclui idade e prioridade calculadas. */
public class CidadaoResponseDTO {

    private Long id;
    private String nome;
    private String cpf;
    private String cartaoSus;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private Boolean gestante;
    private Boolean cronico;
    private Integer idade;
    private Integer prioridade;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getCartaoSus() { return cartaoSus; }
    public void setCartaoSus(String cartaoSus) { this.cartaoSus = cartaoSus; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getGestante() { return gestante; }
    public void setGestante(Boolean gestante) { this.gestante = gestante; }

    public Boolean getCronico() { return cronico; }
    public void setCronico(Boolean cronico) { this.cronico = cronico; }

    public Integer getIdade() { return idade; }
    public void setIdade(Integer idade) { this.idade = idade; }

    public Integer getPrioridade() { return prioridade; }
    public void setPrioridade(Integer prioridade) { this.prioridade = prioridade; }
}
