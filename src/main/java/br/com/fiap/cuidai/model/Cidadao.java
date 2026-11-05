package br.com.fiap.cuidai.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Cidadao atendido pelo CuidAi. Tabela: TB_CIDADAO.
 * Regra do dominio: a prioridade na fila e calculada a partir dos dados do cidadao.
 */
public class Cidadao {

    public static final int IDADE_IDOSO = 60;

    private Long id;
    private String nome;
    private String cpf;
    private String cartaoSus;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private boolean gestante;
    private boolean cronico;

    /** Idade em anos completos. Nao e gravada no banco: e sempre calculada. */
    public Integer getIdade() {
        if (dataNascimento == null) {
            return null;
        }
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    /**
     * Prioridade na fila:
     * 1 = Alta  (idoso 60+ ou gestante)
     * 2 = Media (doenca cronica)
     * 3 = Normal
     */
    public int getPrioridade() {
        Integer idade = getIdade();
        if ((idade != null && idade >= IDADE_IDOSO) || gestante) {
            return 1;
        }
        if (cronico) {
            return 2;
        }
        return 3;
    }

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

    public boolean isGestante() { return gestante; }
    public void setGestante(boolean gestante) { this.gestante = gestante; }

    public boolean isCronico() { return cronico; }
    public void setCronico(boolean cronico) { this.cronico = cronico; }
}
