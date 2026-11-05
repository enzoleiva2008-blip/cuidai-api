package br.com.fiap.cuidai.dao;

import br.com.fiap.cuidai.model.Consulta;
import br.com.fiap.cuidai.model.StatusConsulta;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso a TB_CONSULTA com JDBC puro. As leituras trazem tambem os nomes (JOIN). */
@Repository
public class ConsultaDAO {

    private static final String SELECT_COM_NOMES =
            "SELECT co.cd_consulta, co.cd_cidadao, co.cd_medico, co.cd_especialidade, "
          + "       co.dt_agendamento, co.dt_consulta, co.ds_status, co.nr_prioridade, co.nr_espera_min, "
          + "       ci.nm_cidadao, m.nm_medico, e.nm_especialidade "
          + "  FROM tb_consulta co "
          + "  JOIN tb_cidadao       ci ON ci.cd_cidadao      = co.cd_cidadao "
          + "  JOIN tb_medico        m  ON m.cd_medico        = co.cd_medico "
          + "  JOIN tb_especialidade e  ON e.cd_especialidade = co.cd_especialidade ";

    private final DataSource dataSource;

    public ConsultaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Consulta inserir(Consulta consulta) {
        String sql = "INSERT INTO tb_consulta (cd_consulta, cd_cidadao, cd_medico, cd_especialidade, "
                + "dt_agendamento, dt_consulta, ds_status, nr_prioridade, nr_espera_min) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            Long novoId = proximoId(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, novoId);
                ps.setLong(2, consulta.getCidadaoId());
                ps.setLong(3, consulta.getMedicoId());
                ps.setLong(4, consulta.getEspecialidadeId());
                ps.setTimestamp(5, Timestamp.valueOf(consulta.getDataAgendamento()));
                ps.setTimestamp(6, Timestamp.valueOf(consulta.getDataConsulta()));
                ps.setString(7, consulta.getStatus().name());
                ps.setInt(8, consulta.getPrioridade());
                setInteiroOuNulo(ps, 9, consulta.getEsperaMin());
                ps.executeUpdate();
            }
            consulta.setId(novoId);
            return consulta;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "consulta");
        }
    }

    public List<Consulta> listar() {
        return listarComFiltro(SELECT_COM_NOMES + "ORDER BY co.dt_consulta DESC", null);
    }

    /** Rota aninhada do 1:N: todas as consultas de um cidadao. */
    public List<Consulta> listarPorCidadao(Long cidadaoId) {
        return listarComFiltro(SELECT_COM_NOMES + "WHERE co.cd_cidadao = ? ORDER BY co.dt_consulta DESC", cidadaoId);
    }

    public Optional<Consulta> buscarPorId(Long id) {
        String sql = SELECT_COM_NOMES + "WHERE co.cd_consulta = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "consulta");
        }
    }

    public boolean atualizar(Consulta consulta) {
        String sql = "UPDATE tb_consulta SET cd_medico = ?, cd_especialidade = ?, dt_consulta = ?, "
                + "ds_status = ?, nr_espera_min = ? WHERE cd_consulta = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, consulta.getMedicoId());
            ps.setLong(2, consulta.getEspecialidadeId());
            ps.setTimestamp(3, Timestamp.valueOf(consulta.getDataConsulta()));
            ps.setString(4, consulta.getStatus().name());
            setInteiroOuNulo(ps, 5, consulta.getEsperaMin());
            ps.setLong(6, consulta.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "consulta");
        }
    }

    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_consulta WHERE cd_consulta = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "a consulta");
        }
    }

    /**
     * Usado pela regra de negocio: o medico ja tem consulta AGENDADA nesse horario?
     * @param ignorarId id da propria consulta (na remarcacao), ou null no cadastro
     */
    public boolean existeConsultaNoHorario(Long medicoId, LocalDateTime dataConsulta, Long ignorarId) {
        String sql = "SELECT COUNT(*) FROM tb_consulta "
                + "WHERE cd_medico = ? AND dt_consulta = ? AND ds_status = 'AGENDADA' "
                + "AND cd_consulta <> ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, medicoId);
            ps.setTimestamp(2, Timestamp.valueOf(dataConsulta));
            ps.setLong(3, ignorarId == null ? -1 : ignorarId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "consulta");
        }
    }

    // ---------------------------------------------------------------- auxiliares

    private List<Consulta> listarComFiltro(String sql, Long parametro) {
        List<Consulta> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setLong(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e, "consulta");
        }
    }

    private Long proximoId(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT sq_tb_consulta.NEXTVAL FROM dual");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private void setInteiroOuNulo(PreparedStatement ps, int posicao, Integer valor) throws SQLException {
        if (valor == null) {
            ps.setNull(posicao, Types.INTEGER);
        } else {
            ps.setInt(posicao, valor);
        }
    }

    private Consulta mapear(ResultSet rs) throws SQLException {
        Consulta c = new Consulta();
        c.setId(rs.getLong("cd_consulta"));
        c.setCidadaoId(rs.getLong("cd_cidadao"));
        c.setMedicoId(rs.getLong("cd_medico"));
        c.setEspecialidadeId(rs.getLong("cd_especialidade"));
        c.setDataAgendamento(rs.getTimestamp("dt_agendamento").toLocalDateTime());
        c.setDataConsulta(rs.getTimestamp("dt_consulta").toLocalDateTime());
        c.setStatus(StatusConsulta.valueOf(rs.getString("ds_status")));
        c.setPrioridade(rs.getInt("nr_prioridade"));
        int espera = rs.getInt("nr_espera_min");
        c.setEsperaMin(rs.wasNull() ? null : espera);
        c.setNomeCidadao(rs.getString("nm_cidadao"));
        c.setNomeMedico(rs.getString("nm_medico"));
        c.setNomeEspecialidade(rs.getString("nm_especialidade"));
        return c;
    }
}
