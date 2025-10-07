package com.istoe.demo.repository;

import com.istoe.demo.enums.RoomStatusEnum;
import com.istoe.demo.enums.StatusPernoiteEnum;
import com.istoe.demo.request.CreatePernoiteRequest;
import com.istoe.demo.request.HospedeRequest;
import com.istoe.demo.request.PagamentoRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
public class PernoiteRepository {

    private final JdbcTemplate jdbcTemplate;

    public PernoiteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long adicionarPernoite(CreatePernoiteRequest request) {
        String sqlPernoite = """
                    INSERT INTO pernoite (
                        quarto_id,
                        data_entrada,
                        data_saida,
                        hora_chegada,
                        hora_saida,
                        status_pernoite_enum,
                        ativo
                    )
                    VALUES (?, ?, ?, ?, ?, 0, TRUE)
                    RETURNING id
                """;

        Long pernoiteId = jdbcTemplate.queryForObject(
                sqlPernoite,
                Long.class,
                request.quarto(),
                request.dataEntrada(),
                request.dataSaida(),
                request.horaChegada(),
                request.horaSaida()
        );

        Long fkCategoria = jdbcTemplate.queryForObject(
                "SELECT fk_categoria FROM quarto WHERE id = ?",
                Long.class,
                request.quarto()
        );

        long categoriaId = fkCategoria == null ? 0 : fkCategoria;
        int qtdPessoas = request.hospedes().size();

        Double valorPorPessoa = jdbcTemplate.query(
                """
                        SELECT valor FROM preco_pessoa_categoria
                        WHERE fk_categoria = ? AND qtd_pessoa = ?
                        """,
                ps -> {
                    ps.setLong(1, categoriaId);
                    ps.setInt(2, qtdPessoas);
                },
                rs -> rs.next() ? rs.getDouble("valor") : null
        );

        if (valorPorPessoa == null) valorPorPessoa = 0D;

        LocalDate dataInicio = request.dataEntrada();
        LocalDate dataFim = request.dataSaida();
        double totalPernoite = 0.0;
        int numeroDiaria = 1;

        while (dataInicio.isBefore(dataFim)) {
            LocalDate dataFimDiaria = dataInicio.plusDays(1);

            Long diariaId = inserirDiaria(
                    pernoiteId,
                    dataInicio,
                    dataFimDiaria,
                    valorPorPessoa,
                    qtdPessoas,
                    numeroDiaria,
                    request.quarto()
            );

            totalPernoite += valorPorPessoa;

            if (!request.hospedes().isEmpty()) {
                long qtdRepresentantes = request.hospedes().stream()
                        .filter(HospedeRequest::representante)
                        .count();

                if (qtdRepresentantes > 1) {
                    throw new IllegalArgumentException("Apenas um hóspede pode ser o representante do pernoite.");
                }

                for (HospedeRequest h : request.hospedes()) {
                    jdbcTemplate.update("""
                                INSERT INTO diaria_hospedes (diaria_id, hospedes_id, representante)
                                VALUES (?, ?, ?)
                                ON CONFLICT DO NOTHING
                            """, diariaId, h.id(), h.representante());
                }
            }


            if (request.pagamentoRequestList() != null) {
                for (PagamentoRequest p : request.pagamentoRequestList()) {
                    jdbcTemplate.update("""
                                INSERT INTO diaria_pagamento (
                                    valor,
                                    diaria_id,
                                    data_hora_pagamento,
                                    tipo_pagamento_id
                                )
                                VALUES (?, ?, now(), ?)
                            """, p.valorPagamento(), diariaId, p.tipoPagamento());
                }
            }


            dataInicio = dataInicio.plusDays(1);
            numeroDiaria++;
        }

        jdbcTemplate.update("""
                    UPDATE pernoite
                    SET valot_total = ?
                    WHERE id = ?
                """, totalPernoite, pernoiteId);

        return pernoiteId;
    }

    public void adicionarDiariasAoPernoite(
            Long pernoiteId,
            LocalDate novaDataInicio,
            LocalDate novaDataFim,
            Long quartoId,
            List<HospedeRequest> hospedes,
            List<PagamentoRequest> pagamentos
    ) {
        if (pernoiteId == null || quartoId == null)
            throw new IllegalArgumentException("ID do pernoite e do quarto são obrigatórios.");

        Integer statusQuarto = jdbcTemplate.queryForObject("""
                    SELECT status_quarto_enum FROM quarto WHERE id = ?
                """, Integer.class, quartoId);

        if (statusQuarto == null)
            throw new IllegalStateException("O quarto informado não foi encontrado.");

        RoomStatusEnum status = RoomStatusEnum.fromCodigo(statusQuarto);

        if (status == RoomStatusEnum.OCUPADO
                || status == RoomStatusEnum.RESERVADO
                || status == RoomStatusEnum.DIARIA_ENCERRADA) {
            throw new IllegalStateException("O quarto informado está indisponível para novas diárias.");
        }

        LocalDate ultimaDataFim = jdbcTemplate.queryForObject("""
                    SELECT MAX(data_fim) FROM diaria WHERE pernoite_id = ?
                """, LocalDate.class, pernoiteId);

        int numeroProxima = jdbcTemplate.queryForObject("""
                    SELECT COALESCE(MAX(numero_diaria), 0) + 1 FROM diaria WHERE pernoite_id = ?
                """, Integer.class, pernoiteId);

        Long categoriaId = jdbcTemplate.queryForObject("""
                    SELECT fk_categoria FROM quarto WHERE id = ?
                """, Long.class, quartoId);

        int qtdPessoas = hospedes != null ? hospedes.size() : 1;

        Double valorDiaria = jdbcTemplate.query(
                """
                        SELECT valor FROM preco_pessoa_categoria
                        WHERE fk_categoria = ? AND qtd_pessoa = ?
                        """,
                ps -> {
                    ps.setLong(1, categoriaId);
                    ps.setInt(2, qtdPessoas);
                },
                rs -> rs.next() ? rs.getDouble("valor") : null
        );

        if (valorDiaria == null) valorDiaria = 0D;

        double totalAdicionado = 0.0;
        LocalDate dataAtual = novaDataInicio;

        while (dataAtual.isBefore(novaDataFim)) {
            LocalDate dataFimDiaria = dataAtual.plusDays(1);

            Boolean existe = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*) > 0
                        FROM diaria
                        WHERE pernoite_id = ?
                          AND data_inicio = ?
                    """, Boolean.class, pernoiteId, Date.valueOf(dataAtual));

            boolean podeAdicionar = !Boolean.TRUE.equals(existe)
                    && (ultimaDataFim == null || dataAtual.isAfter(ultimaDataFim) || dataAtual.isEqual(ultimaDataFim));

            if (podeAdicionar) {
                Long diariaId = inserirDiaria(
                        pernoiteId,
                        dataAtual,
                        dataFimDiaria,
                        valorDiaria,
                        qtdPessoas,
                        numeroProxima,
                        quartoId
                );

                totalAdicionado += valorDiaria;

                if (hospedes != null && !hospedes.isEmpty()) {
                    long countRepresentantes = hospedes.stream()
                            .filter(HospedeRequest::representante)
                            .count();

                    if (countRepresentantes > 1) {
                        throw new IllegalArgumentException("Apenas um hóspede pode ser o representante do pernoite.");
                    }

                    for (var h : hospedes) {
                        int rows = jdbcTemplate.update("""
                                    UPDATE diaria_hospedes
                                    SET representante = ?
                                    WHERE diaria_id = ? AND hospedes_id = ?
                                """, h.representante(), diariaId, h.id());

                        if (rows == 0) {
                            jdbcTemplate.update("""
                                        INSERT INTO diaria_hospedes (diaria_id, hospedes_id, representante)
                                        VALUES (?, ?, ?)
                                    """, diariaId, h.id(), h.representante());
                        }
                    }
                }


                if (pagamentos != null) {
                    for (PagamentoRequest p : pagamentos) {
                        jdbcTemplate.update("""
                                    INSERT INTO diaria_pagamento (
                                        valor,
                                        diaria_id,
                                        data_hora_pagamento,
                                        tipo_pagamento_id
                                    )
                                    VALUES (?, ?, now(), ?)
                                """, p.valorPagamento(), diariaId, p.tipoPagamento());
                    }
                }


                numeroProxima++;
            }

            dataAtual = dataAtual.plusDays(1);
        }

        jdbcTemplate.update("""
                    UPDATE pernoite
                    SET data_saida = ?, valot_total = COALESCE(valot_total, 0) + ?
                    WHERE id = ?
                """, novaDataFim, totalAdicionado, pernoiteId);
    }

    private Long inserirDiaria(Long pernoiteId,
                               LocalDate dataInicio,
                               LocalDate dataFim,
                               Double valor,
                               Integer qtdPessoas,
                               Integer numeroDiaria,
                               Long quartoId) {
        String sql = """
                    INSERT INTO diaria (
                        data_inicio,
                        data_fim,
                        valor_diaria,
                        pernoite_id,
                        total,
                        numero_diaria,
                        quantidade_pessoa,
                        quarto_id
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    RETURNING id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                dataInicio,
                dataFim,
                valor,
                pernoiteId,
                valor,
                numeroDiaria,
                qtdPessoas,
                quartoId
        );
    }

    public void cancelarPernoite(Long codPernoite, String motivo) {
        jdbcTemplate.update("""
                    UPDATE pernoite
                    SET ativo = FALSE,
                        status_pernoite_enum = 3
                    WHERE id = ?
                """, codPernoite);
    }

    public List<Map<String, Object>> listarPorStatus(StatusPernoiteEnum status) {
        String sql;
        Object[] params;

        if (status == null) {
            sql = """
            SELECT 
                p.id,
                p.quarto_id,
                q.descricao AS quarto_descricao,
                p.data_entrada,
                p.data_saida,
                p.hora_chegada,
                p.hora_saida,
                p.status_pernoite_enum,
                p.valot_total,
                p.ativo,
                pes.nome AS representante_nome,
                pes.cpf AS representante_cpf
            FROM pernoite p
            JOIN quarto q ON q.id = p.quarto_id
            LEFT JOIN diaria d 
                ON d.pernoite_id = p.id 
                AND CURRENT_DATE BETWEEN d.data_inicio AND d.data_fim
            LEFT JOIN diaria_hospedes dh 
                ON dh.diaria_id = d.id 
                AND dh.representante = TRUE
            LEFT JOIN pessoa pes 
                ON pes.id = dh.hospedes_id
            WHERE CURRENT_DATE BETWEEN p.data_entrada AND p.data_saida
              AND p.ativo = TRUE
            ORDER BY p.data_entrada DESC
        """;
            params = new Object[]{};
        } else {
            sql = """
            SELECT 
                p.id,
                p.quarto_id,
                q.descricao AS quarto_descricao,
                p.data_entrada,
                p.data_saida,
                p.hora_chegada,
                p.hora_saida,
                p.status_pernoite_enum,
                p.valot_total,
                p.ativo,
                pes.nome AS representante_nome,
                pes.cpf AS representante_cpf
            FROM pernoite p
            JOIN quarto q ON q.id = p.quarto_id
            LEFT JOIN diaria d 
                ON d.pernoite_id = p.id 
                AND CURRENT_DATE BETWEEN d.data_inicio AND d.data_fim
            LEFT JOIN diaria_hospedes dh 
                ON dh.diaria_id = d.id 
                AND dh.representante = TRUE
            LEFT JOIN pessoa pes 
                ON pes.id = dh.hospedes_id
            WHERE p.status_pernoite_enum = ?
              AND p.ativo = TRUE
            ORDER BY p.data_entrada DESC
        """;
            params = new Object[]{status.getValue()};
        }

        List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql, params);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<Map<String, Object>> respostaFormatada = new ArrayList<>();
        for (Map<String, Object> row : resultados) {
            Map<String, Object> formatado = new LinkedHashMap<>();

            formatado.put("id", row.get("id"));
            formatado.put("quartoId", row.get("quarto_id"));
            formatado.put("quartoDescricao", row.get("quarto_descricao"));

            Object dataEntradaObj = row.get("data_entrada");
            Object dataSaidaObj = row.get("data_saida");

            formatado.put("dataEntrada", dataEntradaObj instanceof java.sql.Date d ? d.toLocalDate().format(dateFormatter) : null);
            formatado.put("dataSaida", dataSaidaObj instanceof java.sql.Date d ? d.toLocalDate().format(dateFormatter) : null);

            formatado.put("horaChegada", "00:00");
            formatado.put("horaSaida", "00:00");

            Integer statusInt = (Integer) row.get("status_pernoite_enum");
            if (statusInt != null) {
                Optional<StatusPernoiteEnum> statusEnum = Arrays.stream(StatusPernoiteEnum.values())
                        .filter(e -> e.getValue() == statusInt)
                        .findFirst();
                formatado.put("status", statusEnum.map(Enum::name).orElse("DESCONHECIDO"));
            } else {
                formatado.put("status", "DESCONHECIDO");
            }

            formatado.put("valorTotal", row.get("valot_total"));
            formatado.put("ativo", row.get("ativo"));

            // ✅ Nome e CPF do representante
            formatado.put("representanteNome", row.get("representante_nome"));
            formatado.put("representanteCpf", row.get("representante_cpf"));

            respostaFormatada.add(formatado);
        }

        return respostaFormatada;
    }



}
