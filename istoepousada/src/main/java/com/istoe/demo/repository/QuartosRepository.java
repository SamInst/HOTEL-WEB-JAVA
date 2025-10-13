package com.istoe.demo.repository;

import com.istoe.demo.enums.RoomStatusEnum;
import com.istoe.demo.request.CreateQuartoRequest;
import com.istoe.demo.request.UpdateQuartoRequest;
import com.istoe.demo.response.CategoriaResponse;
import com.istoe.demo.response.ObjetoResponse;
import com.istoe.demo.response.RoomsResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class QuartosRepository {

    private final JdbcTemplate jdbcTemplate;

    public QuartosRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public RoomsResponse findRoomsByFilters(LocalDate date, RoomStatusEnum status, String searchTerm) {
        QueryBuilderResult queryResult = buildQueryAndParams(date, status, searchTerm);

        List<RoomWithCategoryInfo> roomsWithCategory = jdbcTemplate.query(
                queryResult.sql,
                queryResult.params.toArray(),
                new RoomRowMapper()
        );

        Map<String, List<RoomsResponse.Categoria.Room>> roomsByCategory = roomsWithCategory.stream()
                .collect(Collectors.groupingBy(
                        rwc -> rwc.categoria != null ? rwc.categoria : "Sem Categoria",
                        Collectors.mapping(rwc -> rwc.room, Collectors.toList())
                ));

        List<RoomsResponse.Categoria> categories = roomsByCategory.entrySet().stream()
                .map(entry -> new RoomsResponse.Categoria(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return new RoomsResponse(categories);
    }

    private QueryBuilderResult buildQueryAndParams(LocalDate date, RoomStatusEnum status, String searchTerm) {
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder();

        // QUERY PRINCIPAL AJUSTADA PARA RELACIONAR HÓSPEDES PELA DIARIA
        sqlBuilder.append("""
            WITH room_status AS (
                SELECT 
                    q.id as quarto_id,
                    q.descricao,
                    q.quantidade_pessoas,
                    q.qtd_cama_casal,
                    q.qtd_cama_solteiro,
                    q.qtd_rede,
                    q.qtd_beliche,
                    q.status_quarto_enum,
                    c.categoria,

                    -- Diaria (agora principal fonte de pessoas)
                    d.id as diaria_id,
                    d.data_inicio,
                    d.data_fim,
                    d.quantidade_pessoa,

                    -- Dados do responsável/hóspede principal na diaria
                    pe.id as pessoa_id,
                    pe.nome,
                    pe.cpf,
                    pe.telefone,

                    ROW_NUMBER() OVER (PARTITION BY q.id ORDER BY d.data_inicio DESC NULLS LAST) as rn

                FROM quarto q
                LEFT JOIN categoria c ON q.fk_categoria = c.id
                LEFT JOIN diaria d ON q.id = d.quarto_id 
                    AND (d.data_inicio <= ? AND d.data_fim >= ?) -- filtro por periodo

                -- JOIN com pessoa do representante na diaria (diaria_hospedes)
                LEFT JOIN diaria_hospedes dh ON d.id = dh.diaria_id AND dh.representante = true
                LEFT JOIN pessoa pe ON dh.hospedes_id = pe.id
                WHERE 1=1
            """);

        // Parâmetros de data (2 vezes)
        params.add(date);
        params.add(date);

        // Filtro de status
        if (status != null) {
            sqlBuilder.append(" AND q.status_quarto_enum = ?");
            params.add(status.ordinal());
        }

        // Filtro de busca por nome/CPF
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sqlBuilder.append(" AND (UPPER(pe.nome) LIKE UPPER(?) OR pe.cpf LIKE ?)");
            String likePattern = "%" + searchTerm + "%";
            params.add(likePattern);
            params.add(likePattern);
        }

        sqlBuilder.append("""
            )
            SELECT * FROM room_status WHERE rn = 1
            ORDER BY categoria, quarto_id
            """);

        return new QueryBuilderResult(sqlBuilder.toString(), params);
    }

    // Classe auxiliar
    private static class QueryBuilderResult {
        final String sql;
        final List<Object> params;
        QueryBuilderResult(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params;
        }
    }

    private static class RoomWithCategoryInfo {
        final String categoria;
        final RoomsResponse.Categoria.Room room;

        RoomWithCategoryInfo(String categoria, RoomsResponse.Categoria.Room room) {
            this.categoria = categoria;
            this.room = room;
        }
    }

    // MAPEAMENTO AJUSTADO PARA HÓSPEDES VIA DIARIA
    private class RoomRowMapper implements RowMapper<RoomWithCategoryInfo> {
        @Override
        public RoomWithCategoryInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            String categoria = rs.getString("categoria");
            RoomStatusEnum roomStatus = determineRoomStatus(rs);

            // Holder representa o hóspede principal da diaria
            RoomsResponse.Categoria.Room.Holder holder = null;
            Long diariaId = (Long) rs.getObject("diaria_id");
            if (diariaId != null) {
                holder = findDiariaHolder(diariaId);
            }

            // Dados do Room
            RoomsResponse.Categoria.Room room = new RoomsResponse.Categoria.Room(
                    rs.getLong("quarto_id"),
                    roomStatus,
                    rs.getInt("quantidade_pessoa"),
                    rs.getInt("qtd_cama_solteiro"),
                    rs.getInt("qtd_cama_casal"),
                    rs.getInt("qtd_beliche"),
                    rs.getInt("qtd_rede"),
                    holder,
                    null // dayUse, adicione lógica se for usar
            );
            return new RoomWithCategoryInfo(categoria, room);
        }

        private RoomStatusEnum determineRoomStatus(ResultSet rs) throws SQLException {
            Integer statusEnum = (Integer) rs.getObject("status_quarto_enum");
            return statusEnum != null ? RoomStatusEnum.values()[statusEnum] : RoomStatusEnum.DISPONIVEL;
        }
    }

    // Novo método para buscar responsável na diária
    private RoomsResponse.Categoria.Room.Holder findDiariaHolder(Long diariaId) {
        if (diariaId == null) return null;
        String sql = """
            SELECT 
                pe.id, pe.nome, pe.cpf, pe.telefone
            FROM diaria_hospedes dh
            JOIN pessoa pe ON dh.hospedes_id = pe.id
            WHERE dh.diaria_id = ? AND dh.representante = true
            LIMIT 1
        """;
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{diariaId}, (rs, rowNum) ->
                    new RoomsResponse.Categoria.Room.Holder(
                            (Long) rs.getObject("id"),
                            rs.getString("nome"),
                            rs.getString("cpf"),
                            rs.getString("telefone"),
                            null, // acompanhantes, ajuste se necessário
                            null, null, null, null // datas/horas, ajuste se necessário
                    )
            );
        } catch (Exception e) {
            System.err.println("Erro ao buscar representante da diaria " + diariaId + ": " + e.getMessage());
            return null;
        }
    }

    public List<ObjetoResponse> listarQuartosEnum() {
        String sql = "SELECT id, descricao FROM quarto order by id";
        return jdbcTemplate.query(sql, ObjetoResponse.ROW_MAPPER);
    }

    public List<CategoriaResponse> listarCategorias() {
        final String sql = "SELECT id, categoria FROM categoria ORDER BY categoria";
        return jdbcTemplate.query(sql, (rs, i) ->
                new CategoriaResponse(rs.getLong("id"), rs.getString("categoria"))
        );
    }

    public Long inserirQuarto(CreateQuartoRequest req) {
        final String sql = """
            INSERT INTO quarto (
                descricao,
                quantidade_pessoas,
                status_quarto_enum,
                qtd_cama_casal,
                qtd_cama_solteiro,
                qtd_rede,
                qtd_beliche,
                fk_categoria
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, req.descricao());
            ps.setObject(2, req.quantidadePessoas());
            ps.setObject(3, req.statusCodigo());
            ps.setObject(4, req.qtdCamaCasal());
            ps.setObject(5, req.qtdCamaSolteiro());
            ps.setObject(6, req.qtdRede());
            ps.setObject(7, req.qtdBeliche());
            ps.setObject(8, req.categoriaId());
            return ps;
        }, kh);

        return Objects.requireNonNull(kh.getKey()).longValue();
    }

    public int atualizarQuarto(Long id, UpdateQuartoRequest req) {
        final String sql = """
            UPDATE quarto
               SET descricao = ?,
                   quantidade_pessoas = ?,
                   status_quarto_enum = ?,
                   qtd_cama_casal = ?,
                   qtd_cama_solteiro = ?,
                   qtd_rede = ?,
                   qtd_beliche = ?,
                   fk_categoria = ?
             WHERE id = ?
        """;
        return jdbcTemplate.update(sql,
                req.descricao(),
                req.quantidadePessoas(),
                req.statusCodigo(),
                req.qtdCamaCasal(),
                req.qtdCamaSolteiro(),
                req.qtdRede(),
                req.qtdBeliche(),
                req.categoriaId(),
                id
        );
    }
}
