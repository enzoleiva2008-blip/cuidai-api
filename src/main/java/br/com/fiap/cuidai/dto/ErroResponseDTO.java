package br.com.fiap.cuidai.dto;

import java.time.LocalDateTime;
import java.util.List;

/** JSON padronizado de erro. Nunca contem stack trace nem SQL. */
public class ErroResponseDTO {

    private final LocalDateTime timestamp;
    private final int status;
    private final String erro;
    private final String mensagem;
    private final String caminho;
    private final List<String> campos;

    public ErroResponseDTO(int status, String erro, String mensagem, String caminho, List<String> campos) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.caminho = caminho;
        this.campos = campos;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getErro() { return erro; }
    public String getMensagem() { return mensagem; }
    public String getCaminho() { return caminho; }
    public List<String> getCampos() { return campos; }
}
