package br.com.fiap.cuidai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Dados que o front envia para cadastrar ou atualizar um cidadao. */
public class CidadaoRequestDTO {

    @NotBlank(message = "O nome e obrigatorio")
    @Size(max = 80, message = "O nome deve ter no maximo 80 caracteres")
    private String nome;

    @NotBlank(message = "O CPF e obrigatorio")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve ter 11 numeros, sem pontos ou traco")
    private String cpf;

    @NotBlank(message = "O Cartao SUS e obrigatorio")
    @Pattern(regexp = "\\d{15}", message = "O Cartao SUS deve ter 15 numeros")
    private String cartaoSus;

    @NotNull(message = "A data de nascimento e obrigatoria")
    @Past(message = "A data de nascimento deve estar no passado")
    private LocalDate dataNascimento;

    @NotBlank(message = "O telefone e obrigatorio")
    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve ter DDD + numero, so numeros (10 ou 11 digitos)")
    private String telefone;

    @Email(message = "E-mail invalido")
    @Size(max = 100, message = "O e-mail deve ter no maximo 100 caracteres")
    private String email;

    @NotNull(message = "Informe se e gestante (true/false)")
    private Boolean gestante;

    @NotNull(message = "Informe se tem doenca cronica (true/false)")
    private Boolean cronico;

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
}
