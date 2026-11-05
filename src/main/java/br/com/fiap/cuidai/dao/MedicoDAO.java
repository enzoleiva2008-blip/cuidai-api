package br.com.fiap.cuidai.dao;

import br.com.fiap.cuidai.model.Medico;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso a TB_MEDICO (e consulta a associativa TB_MEDICO_ESPECIALIDADE) com JDBC puro. */
@Repository
public class MedicoDAO {

    private static final String COLUNAS = "cd_medico, cd_ubs, nm_medico, nr_crm, fl_ativo";

    private final DataSource dataSource;

    public MedicoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Medico inserir(Medico medico) {
        String sql = "INSERT INTO tb_medico (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            Long novoId = proximoId(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, novoId);
                ps.setLong(2, medico.getUbsId());
                ps.setString(3, medico.getNome());
                ps.setString(4, medico.getCrm());
                ps.setString(5, medico.isAtivo() ? "S" : "N");
                ps.executeUpdate();
            }
            medico.setId(novoId);
            return medico;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "medico");
        }
    }

    public List<Medico> listar() {
        String sql = "SELECT " + COLUNAS + " FROM tb_medico ORDER BY nm_medico";
        List<Medico> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "medico");
        }
    }

    public Optional<Medico> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM tb_medico WHERE cd_medico = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "medico");
        }
    }

    public boolean atualizar(Medico medico) {
        String sql = "UPDATE tb_medico SET cd_ubs = ?, nm_medico = ?, nr_crm = ?, fl_ativo = ? WHERE cd_medico = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, medico.getUbsId());
            ps.setString(2, medico.getNome());
            ps.setString(3, medico.getCrm());
            ps.setString(4, medico.isAtivo() ? "S" : "N");
            ps.setLong(5, medico.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "medico");
        }
    }

    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_medico WHERE cd_medico = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "o medico");
        }
    }

    /** Consulta a tabela associativa N:N: o medico atende essa especialidade? */
    public boolean atendeEspecialidade(Long medicoId, Long especialidadeId) {
        String sql = "SELECT COUNT(*) FROM tb_medico_especialidade WHERE cd_medico = ? AND cd_especialidade = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, medicoId);
            ps.setLong(2, especialidadeId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "medico");
        }
    }

    // ---------------------------------------------------------------- auxiliares

    private Long proximoId(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT sq_tb_medico.NEXTVAL FROM dual");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private Medico mapear(ResultSet rs) throws SQLException {
        Medico m = new Medico();
        m.setId(rs.getLong("cd_medico"));
        m.setUbsId(rs.getLong("cd_ubs"));
        m.setNome(rs.getString("nm_medico"));
        m.setCrm(rs.getString("nr_crm"));
        m.setAtivo("S".equals(rs.getString("fl_ativo")));
        return m;
    }
}
