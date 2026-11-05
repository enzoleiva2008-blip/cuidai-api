package br.com.fiap.cuidai.dao;

import br.com.fiap.cuidai.model.Cidadao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso a TB_CIDADAO com JDBC puro (PreparedStatement). */
@Repository
public class CidadaoDAO {

    private static final String COLUNAS =
            "cd_cidadao, nm_cidadao, nr_cpf, nr_cartao_sus, dt_nascimento, nr_telefone, ds_email, fl_gestante, fl_cronico";

    private final DataSource dataSource;

    public CidadaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Cidadao inserir(Cidadao cidadao) {
        String sql = "INSERT INTO tb_cidadao (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            Long novoId = proximoId(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, novoId);
                preencherCampos(ps, cidadao, 2);
                ps.executeUpdate();
            }
            cidadao.setId(novoId);
            return cidadao;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "cidadao");
        }
    }

    public List<Cidadao> listar() {
        String sql = "SELECT " + COLUNAS + " FROM tb_cidadao ORDER BY nm_cidadao";
        List<Cidadao> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "cidadao");
        }
    }

    public Optional<Cidadao> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM tb_cidadao WHERE cd_cidadao = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "cidadao");
        }
    }

    /** @return true se atualizou; false se o id nao existe */
    public boolean atualizar(Cidadao cidadao) {
        String sql = "UPDATE tb_cidadao SET nm_cidadao = ?, nr_cpf = ?, nr_cartao_sus = ?, dt_nascimento = ?, "
                + "nr_telefone = ?, ds_email = ?, fl_gestante = ?, fl_cronico = ? WHERE cd_cidadao = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            preencherCampos(ps, cidadao, 1);
            ps.setLong(9, cidadao.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "cidadao");
        }
    }

    /** @return true se removeu; false se o id nao existe */
    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_cidadao WHERE cd_cidadao = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "o cidadao");
        }
    }

    // ---------------------------------------------------------------- auxiliares

    private Long proximoId(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT sq_tb_cidadao.NEXTVAL FROM dual");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /** Preenche os 8 campos (sem o id) a partir da posicao informada. */
    private void preencherCampos(PreparedStatement ps, Cidadao c, int inicio) throws SQLException {
        ps.setString(inicio, c.getNome());
        ps.setString(inicio + 1, c.getCpf());
        ps.setString(inicio + 2, c.getCartaoSus());
        ps.setDate(inicio + 3, Date.valueOf(c.getDataNascimento()));
        ps.setString(inicio + 4, c.getTelefone());
        ps.setString(inicio + 5, c.getEmail());
        ps.setString(inicio + 6, c.isGestante() ? "S" : "N");
        ps.setString(inicio + 7, c.isCronico() ? "S" : "N");
    }

    private Cidadao mapear(ResultSet rs) throws SQLException {
        Cidadao c = new Cidadao();
        c.setId(rs.getLong("cd_cidadao"));
        c.setNome(rs.getString("nm_cidadao"));
        c.setCpf(rs.getString("nr_cpf"));
        c.setCartaoSus(rs.getString("nr_cartao_sus"));
        c.setDataNascimento(rs.getDate("dt_nascimento").toLocalDate());
        c.setTelefone(rs.getString("nr_telefone"));
        c.setEmail(rs.getString("ds_email"));
        c.setGestante("S".equals(rs.getString("fl_gestante")));
        c.setCronico("S".equals(rs.getString("fl_cronico")));
        return c;
    }
}
