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
                    
                    -- Dados do pernoite ativo
                    p.id as pernoite_id,
                    p.data_entrada,
                    p.data_saida,
                    p.hora_chegada,
                    p.hora_saida,
                    p.quantidade_pessoa as acompanhantes,
                    
                    -- Dados da pessoa (representante)
                    pe.id as pessoa_id,
                    pe.nome,
                    pe.cpf,
                    pe.telefone,
                    
                    -- Dados de diária encerrada (day use)
                    d.id as diaria_id,
                    d.data_inicio as diaria_data_inicio,
                    d.data_fim as diaria_data_fim,
                    
                    ROW_NUMBER() OVER (PARTITION BY q.id ORDER BY p.data_entrada DESC NULLS LAST) as rn
                    
                FROM quarto q
                LEFT JOIN categoria c ON q.fk_categoria = c.id
                LEFT JOIN pernoite p ON q.id = p.quarto_id 
                    AND p.ativo = true 
                    AND (p.data_entrada <= ? AND p.data_saida >= ?)
                LEFT JOIN diaria d ON p.id = d.pernoite_id
                LEFT JOIN pessoa pe ON p.id = (
                    SELECT p2.id 
                    FROM pernoite p2 
                    WHERE p2.quarto_id = q.id 
                    AND p2.ativo = true
                    AND p2.data_entrada <= ? AND p2.data_saida >= ?
                    ORDER BY p2.data_entrada ASC
                    LIMIT 1
                )
                WHERE 1=1
            """);

        // Parâmetros para verificação de data no pernoite (4 vezes)
        for (int i = 0; i < 4; i++) {
            params.add(date);
        }

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

    // Classe auxiliar para retornar SQL e parâmetros
    private static class QueryBuilderResult {
        final String sql;
        final List<Object> params;

        QueryBuilderResult(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params;
        }
    }

    // Classe auxiliar apenas para carregar categoria junto com room
    private static class RoomWithCategoryInfo {
        final String categoria;
        final RoomsResponse.Categoria.Room room;

        RoomWithCategoryInfo(String categoria, RoomsResponse.Categoria.Room room) {
            this.categoria = categoria;
            this.room = room;
        }
    }

    private class RoomRowMapper implements RowMapper<RoomWithCategoryInfo> {
        @Override
        public RoomWithCategoryInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            String categoria = rs.getString("categoria");

            RoomStatusEnum roomStatus = determineRoomStatus(rs);

            Long pernoiteId = (Long) rs.getObject("pernoite_id");
            RoomsResponse.Categoria.Room.Holder holder = null;

            if (pernoiteId != null && (roomStatus == RoomStatusEnum.OCUPADO || roomStatus == RoomStatusEnum.RESERVADO)) {
                holder = findPersonHolder(pernoiteId);
            }

            RoomsResponse.Categoria.Room.DayUse dayUse = null;
            Long diariaId = (Long) rs.getObject("diaria_id");

            if (roomStatus == RoomStatusEnum.DIARIA_ENCERRADA && diariaId != null) {
                dayUse = new RoomsResponse.Categoria.Room.DayUse(
                        diariaId,
                        rs.getDate("diaria_data_inicio") != null ? rs.getDate("diaria_data_inicio").toLocalDate() : null,
                        rs.getTime("hora_chegada") != null ? rs.getTime("hora_chegada").toLocalTime() : null,
                        rs.getTime("hora_saida") != null ? rs.getTime("hora_saida").toLocalTime() : null
                );
            }

            RoomsResponse.Categoria.Room room = new RoomsResponse.Categoria.Room(
                    rs.getLong("quarto_id"),
                    roomStatus,
                    rs.getInt("quantidade_pessoas"),
                    rs.getInt("qtd_cama_solteiro"),
                    rs.getInt("qtd_cama_casal"),
                    rs.getInt("qtd_beliche"),
                    rs.getInt("qtd_rede"),
                    holder,
                    dayUse
            );

            return new RoomWithCategoryInfo(categoria, room);
        }

        private RoomStatusEnum determineRoomStatus(ResultSet rs) throws SQLException {
            Integer statusEnum = (Integer) rs.getObject("status_quarto_enum");

            if (statusEnum != null) {
                return RoomStatusEnum.values()[statusEnum];
            }

            LocalDate today = LocalDate.now();
            Long pernoiteId = (Long) rs.getObject("pernoite_id");

            if (pernoiteId != null) {
                LocalDate dataEntrada = rs.getDate("data_entrada") != null ?
                        rs.getDate("data_entrada").toLocalDate() : null;
                LocalDate dataSaida = rs.getDate("data_saida") != null ?
                        rs.getDate("data_saida").toLocalDate() : null;

                if (dataEntrada != null && dataSaida != null) {
                    if (dataEntrada.isAfter(today)) {
                        return RoomStatusEnum.RESERVADO;
                    } else if (!dataEntrada.isAfter(today) && !dataSaida.isBefore(today)) {
                        return RoomStatusEnum.OCUPADO;
                    } else if (dataSaida.isBefore(today)) {
                        return RoomStatusEnum.DIARIA_ENCERRADA;
                    }
                }
            }

            return RoomStatusEnum.DISPONIVEL;
        }
    }

    public List<ObjetoResponse> listarQuartosEnum() {
        String sql = "SELECT id, descricao FROM quarto order by id";
        return jdbcTemplate.query(sql, ObjetoResponse.ROW_MAPPER);
    }


    private RoomsResponse.Categoria.Room.Holder findPersonHolder(Long pernoiteId) {
        if (pernoiteId == null) return null;

        String sql = """
        SELECT pe.id, pe.nome, pe.cpf, pe.telefone, p.quantidade_pessoa as acompanhantes,
               p.data_entrada, p.data_saida, p.hora_chegada, p.hora_saida
        FROM pernoite p
        JOIN pernoite_pessoas pp
          ON pp.pernoite_id = p.id
         AND pp.representante = TRUE
        JOIN pessoa pe
          ON pe.id = pp.pessoas_id
        WHERE p.id = ? AND p.ativo = true
        """;

        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{pernoiteId}, (rs, rowNum) ->
                    new RoomsResponse.Categoria.Room.Holder(
                            (Long) rs.getObject("id"),
                            rs.getString("nome"),
                            rs.getString("cpf"),
                            rs.getString("telefone"),
                            rs.getInt("acompanhantes"),
                            rs.getDate("data_entrada") != null ? rs.getDate("data_entrada").toLocalDate() : null,
                            rs.getDate("data_saida") != null ? rs.getDate("data_saida").toLocalDate() : null,
                            rs.getTime("hora_chegada") != null ? rs.getTime("hora_chegada").toLocalTime() : null,
                            rs.getTime("hora_saida") != null ? rs.getTime("hora_saida").toLocalTime() : null
                    )
            );
        } catch (Exception e) {
            System.err.println("Erro ao buscar representante do pernoite " + pernoiteId + ": " + e.getMessage());
            return null;
        }
    }

    public List<CategoriaResponse> listarCategorias() {
        final String sql = """
            SELECT id, categoria
            FROM categoria
            ORDER BY categoria
        """;
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
            ps.setObject(3, req.statusCodigo()); // SMALLINT no banco (int2) – ok passar int
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