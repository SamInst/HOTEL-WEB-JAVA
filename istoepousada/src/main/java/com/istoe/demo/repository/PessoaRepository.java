package com.istoe.demo.repository;

import com.istoe.demo.response.Empresa;
import com.istoe.demo.response.Pessoa;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class PessoaRepository {

    private final JdbcTemplate jdbcTemplate;

    public PessoaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final ResultSetExtractor<List<Pessoa>> PESSOA_COM_EMPRESAS_EXTRACTOR = rs -> {
        Map<Long, Pessoa> pessoaMap = new LinkedHashMap<>();
        Map<Long, List<Empresa>> empresasPorPessoa = new HashMap<>();

        while (rs.next()) {
            Long pessoaId = rs.getLong("id");

            if (!pessoaMap.containsKey(pessoaId)) {
                Pessoa pessoa = mapPessoaCompleta(rs);
                pessoaMap.put(pessoaId, pessoa);
                empresasPorPessoa.put(pessoaId, new ArrayList<>());
            }

            Long empresaId = rs.getObject("empresa_id", Long.class);
            if (empresaId != null) {
                Empresa empresa = mapEmpresaSimples(rs, "empresa_");
                empresasPorPessoa.get(pessoaId).add(empresa);
            }
        }

        return pessoaMap.entrySet().stream()
                .map(entry -> entry.getValue().withEmpresas(empresasPorPessoa.get(entry.getKey())))
                .toList();
    };

    private Pessoa mapPessoaCompleta(ResultSet rs) throws SQLException {
        return new Pessoa(
                rs.getLong("id"),
                rs.getTimestamp("data_hora_cadastro") != null ?
                        rs.getTimestamp("data_hora_cadastro").toLocalDateTime() : null,
                rs.getString("nome"),
                rs.getDate("data_nascimento") != null ?
                        rs.getDate("data_nascimento").toLocalDate() : null,
                rs.getString("cpf"),
                rs.getString("rg"),
                rs.getString("email"),
                rs.getString("telefone"),
                rs.getObject("fk_pais", Long.class),
                rs.getObject("fk_estado", Long.class),
                rs.getObject("fk_municipio", Long.class),
                rs.getString("endereco"),
                rs.getString("complemento"),
                rs.getObject("hospedado", Boolean.class),
                rs.getObject("vezes_hospedado", Integer.class),
                rs.getObject("cliente_novo", Boolean.class),
                rs.getString("cep"),
                rs.getObject("idade", Integer.class),
                rs.getString("bairro"),
                rs.getObject("sexo", Short.class),
                rs.getString("numero"),
                List.of()
        );
    }

    private Empresa mapEmpresaSimples(ResultSet rs, String prefix) throws SQLException {
        return new Empresa(
                rs.getLong(prefix + "id"),
                rs.getString(prefix + "razao_social"),
                rs.getString(prefix + "nome_fantasia"),
                rs.getString(prefix + "cnpj"),
                rs.getString(prefix + "inscricao_estadual"),
                rs.getString(prefix + "inscricao_municipal"),
                rs.getString(prefix + "telefone"),
                rs.getString(prefix + "email"),
                rs.getString(prefix + "endereco"),
                rs.getString(prefix + "cep"),
                rs.getString(prefix + "numero"),
                rs.getString(prefix + "complemento"),
                rs.getObject(prefix + "fk_pais", Long.class),
                rs.getObject(prefix + "fk_estado", Long.class),
                rs.getObject(prefix + "fk_municipio", Long.class),
                rs.getString(prefix + "bairro"),
                rs.getString(prefix + "tipo_empresa"),
                rs.getBoolean(prefix + "ativa"),
                List.of()
        );
    }

    public Optional<Pessoa> findById(Long id) {
        String sql = """
            SELECT 
                p.*,
                e.id as empresa_id,
                e.razao_social as empresa_razao_social,
                e.nome_fantasia as empresa_nome_fantasia,
                e.cnpj as empresa_cnpj,
                e.inscricao_estadual as empresa_inscricao_estadual,
                e.inscricao_municipal as empresa_inscricao_municipal,
                e.telefone as empresa_telefone,
                e.email as empresa_email,
                e.endereco as empresa_endereco,
                e.cep as empresa_cep,
                e.numero as empresa_numero,
                e.complemento as empresa_complemento,
                e.fk_pais as empresa_fk_pais,
                e.fk_estado as empresa_fk_estado,
                e.fk_municipio as empresa_fk_municipio,
                e.bairro as empresa_bairro,
                e.tipo_empresa as empresa_tipo_empresa,
                e.ativa as empresa_ativa
            FROM pessoa p
            LEFT JOIN empresa_pessoa ep ON p.id = ep.fk_pessoa
            LEFT JOIN empresa e ON ep.fk_empresa = e.id
            WHERE p.id = ?
            ORDER BY e.razao_social
        """;

        List<Pessoa> pessoas = jdbcTemplate.query(sql, PESSOA_COM_EMPRESAS_EXTRACTOR, id);
        return pessoas.isEmpty() ? Optional.empty() : Optional.of(pessoas.get(0));
    }

    public List<Pessoa> findByNome(String nome) {
        String sql = """
            SELECT 
                p.*,
                e.id as empresa_id,
                e.razao_social as empresa_razao_social,
                e.nome_fantasia as empresa_nome_fantasia,
                e.cnpj as empresa_cnpj,
                e.inscricao_estadual as empresa_inscricao_estadual,
                e.inscricao_municipal as empresa_inscricao_municipal,
                e.telefone as empresa_telefone,
                e.email as empresa_email,
                e.endereco as empresa_endereco,
                e.cep as empresa_cep,
                e.numero as empresa_numero,
                e.complemento as empresa_complemento,
                e.fk_pais as empresa_fk_pais,
                e.fk_estado as empresa_fk_estado,
                e.fk_municipio as empresa_fk_municipio,
                e.bairro as empresa_bairro,
                e.tipo_empresa as empresa_tipo_empresa,
                e.ativa as empresa_ativa
            FROM pessoa p
            LEFT JOIN empresa_pessoa ep ON p.id = ep.fk_pessoa
            LEFT JOIN empresa e ON ep.fk_empresa = e.id
            WHERE p.nome ILIKE ?
            ORDER BY p.nome, e.razao_social
        """;

        return jdbcTemplate.query(sql, PESSOA_COM_EMPRESAS_EXTRACTOR, "%" + nome + "%");
    }

    public Optional<Pessoa> findByCpf(String cpf) {
        String sql = """
            SELECT 
                p.*,
                e.id as empresa_id,
                e.razao_social as empresa_razao_social,
                e.nome_fantasia as empresa_nome_fantasia,
                e.cnpj as empresa_cnpj,
                e.inscricao_estadual as empresa_inscricao_estadual,
                e.inscricao_municipal as empresa_inscricao_municipal,
                e.telefone as empresa_telefone,
                e.email as empresa_email,
                e.endereco as empresa_endereco,
                e.cep as empresa_cep,
                e.numero as empresa_numero,
                e.complemento as empresa_complemento,
                e.fk_pais as empresa_fk_pais,
                e.fk_estado as empresa_fk_estado,
                e.fk_municipio as empresa_fk_municipio,
                e.bairro as empresa_bairro,
                e.tipo_empresa as empresa_tipo_empresa,
                e.ativa as empresa_ativa
            FROM pessoa p
            LEFT JOIN empresa_pessoa ep ON p.id = ep.fk_pessoa
            LEFT JOIN empresa e ON ep.fk_empresa = e.id
            WHERE p.cpf = ?
            ORDER BY e.razao_social
        """;

        List<Pessoa> pessoas = jdbcTemplate.query(sql, PESSOA_COM_EMPRESAS_EXTRACTOR, cpf);
        return pessoas.isEmpty() ? Optional.empty() : Optional.of(pessoas.get(0));
    }

    public List<Pessoa> findByNomeOrCpf(String termo) {
        String sql = """
            SELECT 
                p.*,
                e.id as empresa_id,
                e.razao_social as empresa_razao_social,
                e.nome_fantasia as empresa_nome_fantasia,
                e.cnpj as empresa_cnpj,
                e.inscricao_estadual as empresa_inscricao_estadual,
                e.inscricao_municipal as empresa_inscricao_municipal,
                e.telefone as empresa_telefone,
                e.email as empresa_email,
                e.endereco as empresa_endereco,
                e.cep as empresa_cep,
                e.numero as empresa_numero,
                e.complemento as empresa_complemento,
                e.fk_pais as empresa_fk_pais,
                e.fk_estado as empresa_fk_estado,
                e.fk_municipio as empresa_fk_municipio,
                e.bairro as empresa_bairro,
                e.tipo_empresa as empresa_tipo_empresa,
                e.ativa as empresa_ativa
            FROM pessoa p
            LEFT JOIN empresa_pessoa ep ON p.id = ep.fk_pessoa
            LEFT JOIN empresa e ON ep.fk_empresa = e.id
            WHERE p.nome ILIKE ? OR p.cpf = ?
            ORDER BY p.nome, e.razao_social
        """;

        return jdbcTemplate.query(sql, PESSOA_COM_EMPRESAS_EXTRACTOR, "%" + termo + "%", termo);
    }

    @Transactional
    public Pessoa save(Pessoa pessoa) {
        if (pessoa.id() == null) {
            return insert(pessoa);
        } else {
            update(pessoa);
            return findById(pessoa.id()).orElse(pessoa);
        }
    }

    private Pessoa insert(Pessoa pessoa) {
        String sql = """
        INSERT INTO pessoa (
            data_hora_cadastro, nome, data_nascimento, cpf, rg, email, 
            telefone, fk_pais, fk_estado, fk_municipio, endereco, 
            complemento, hospedado, vezes_hospedado, cliente_novo, 
            cep, idade, bairro, sexo, numero
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        LocalDateTime dataHoraCadastro = pessoa.dataHoraCadastro();
        if (dataHoraCadastro == null) dataHoraCadastro = LocalDateTime.now();

        final LocalDateTime finalDataHoraCadastro = dataHoraCadastro;
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int idx = 1;
            ps.setTimestamp(idx++, Timestamp.valueOf(finalDataHoraCadastro));
            ps.setString(idx++, pessoa.nome());
            ps.setDate(idx++, pessoa.dataNascimento() != null ?
                    Date.valueOf(pessoa.dataNascimento()) : null);
            ps.setString(idx++, pessoa.cpf());
            ps.setString(idx++, pessoa.rg());
            ps.setString(idx++, pessoa.email());
            ps.setString(idx++, pessoa.telefone());
            ps.setObject(idx++, pessoa.fkPais());
            ps.setObject(idx++, pessoa.fkEstado());
            ps.setObject(idx++, pessoa.fkMunicipio());
            ps.setString(idx++, pessoa.endereco());
            ps.setString(idx++, pessoa.complemento());
            ps.setObject(idx++, pessoa.hospedado());
            ps.setObject(idx++, pessoa.vezesHospedado());
            ps.setObject(idx++, pessoa.clienteNovo());
            ps.setString(idx++, pessoa.cep());
            ps.setObject(idx++, pessoa.idade());
            ps.setString(idx++, pessoa.bairro());
            ps.setObject(idx++, pessoa.sexo());
            ps.setString(idx++, pessoa.numero());
            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        Long generatedId = keys != null && keys.containsKey("id")
                ? ((Number) keys.get("id")).longValue()
                : null;

        return pessoa.withId(generatedId);
    }

    @Transactional
    public void update(Pessoa pessoa) {
        String sql = """
            UPDATE pessoa SET
                nome = ?, data_nascimento = ?, cpf = ?, rg = ?, email = ?,
                telefone = ?, fk_pais = ?, fk_estado = ?, fk_municipio = ?,
                endereco = ?, complemento = ?, hospedado = ?, vezes_hospedado = ?,
                cliente_novo = ?, cep = ?, idade = ?, bairro = ?, sexo = ?, numero = ?
            WHERE id = ?
        """;

        jdbcTemplate.update(sql,
                pessoa.nome(),
                pessoa.dataNascimento() != null ? Date.valueOf(pessoa.dataNascimento()) : null,
                pessoa.cpf(),
                pessoa.rg(),
                pessoa.email(),
                pessoa.telefone(),
                pessoa.fkPais(),
                pessoa.fkEstado(),
                pessoa.fkMunicipio(),
                pessoa.endereco(),
                pessoa.complemento(),
                pessoa.hospedado(),
                pessoa.vezesHospedado(),
                pessoa.clienteNovo(),
                pessoa.cep(),
                pessoa.idade(),
                pessoa.bairro(),
                pessoa.sexo(),
                pessoa.numero(),
                pessoa.id()
        );
    }

    @Transactional
    public void setHospedado(Long id, Boolean hospedado) {
        String sql = "UPDATE pessoa SET hospedado = ? WHERE id = ?";
        jdbcTemplate.update(sql, hospedado, id);
    }

    @Transactional
    public void incrementarHospedagem(Long id) {
        String sql = """
            UPDATE pessoa 
            SET vezes_hospedado = COALESCE(vezes_hospedado, 0) + 1,
                cliente_novo = false
            WHERE id = ?
        """;
        jdbcTemplate.update(sql, id);
    }

    public List<Pessoa> findAll() {
        String sql = """
            SELECT 
                p.*,
                e.id as empresa_id,
                e.razao_social as empresa_razao_social,
                e.nome_fantasia as empresa_nome_fantasia,
                e.cnpj as empresa_cnpj,
                e.inscricao_estadual as empresa_inscricao_estadual,
                e.inscricao_municipal as empresa_inscricao_municipal,
                e.telefone as empresa_telefone,
                e.email as empresa_email,
                e.endereco as empresa_endereco,
                e.cep as empresa_cep,
                e.numero as empresa_numero,
                e.complemento as empresa_complemento,
                e.fk_pais as empresa_fk_pais,
                e.fk_estado as empresa_fk_estado,
                e.fk_municipio as empresa_fk_municipio,
                e.bairro as empresa_bairro,
                e.tipo_empresa as empresa_tipo_empresa,
                e.ativa as empresa_ativa
            FROM pessoa p
            LEFT JOIN empresa_pessoa ep ON p.id = ep.fk_pessoa
            LEFT JOIN empresa e ON ep.fk_empresa = e.id
            ORDER BY p.nome, e.razao_social
        """;

        return jdbcTemplate.query(sql, PESSOA_COM_EMPRESAS_EXTRACTOR);
    }

    public List<Pessoa> findHospedados() {
        String sql = """
            SELECT 
                p.*,
                e.id as empresa_id,
                e.razao_social as empresa_razao_social,
                e.nome_fantasia as empresa_nome_fantasia,
                e.cnpj as empresa_cnpj,
                e.inscricao_estadual as empresa_inscricao_estadual,
                e.inscricao_municipal as empresa_inscricao_municipal,
                e.telefone as empresa_telefone,
                e.email as empresa_email,
                e.endereco as empresa_endereco,
                e.cep as empresa_cep,
                e.numero as empresa_numero,
                e.complemento as empresa_complemento,
                e.fk_pais as empresa_fk_pais,
                e.fk_estado as empresa_fk_estado,
                e.fk_municipio as empresa_fk_municipio,
                e.bairro as empresa_bairro,
                e.tipo_empresa as empresa_tipo_empresa,
                e.ativa as empresa_ativa
            FROM pessoa p
            LEFT JOIN empresa_pessoa ep ON p.id = ep.fk_pessoa
            LEFT JOIN empresa e ON ep.fk_empresa = e.id
            WHERE p.hospedado = true
            ORDER BY p.nome, e.razao_social
        """;

        return jdbcTemplate.query(sql, PESSOA_COM_EMPRESAS_EXTRACTOR);
    }

    public boolean possuiVinculoComEmpresa(Long pessoaId) {
        String sql = "SELECT COUNT(*) FROM empresa_pessoa WHERE fk_pessoa = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, pessoaId);
        return count != null && count > 0;
    }

    public int contarEmpresasVinculadas(Long pessoaId) {
        String sql = "SELECT COUNT(*) FROM empresa_pessoa WHERE fk_pessoa = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, pessoaId);
        return count != null ? count : 0;
    }

    public boolean isVinculadaComEmpresa(Long pessoaId, Long empresaId) {
        String sql = """
            SELECT COUNT(*) FROM empresa_pessoa
            WHERE fk_pessoa = ? AND fk_empresa = ?
        """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, pessoaId, empresaId);
        return count != null && count > 0;
    }

    @Transactional
    public void removerTodosVinculosComEmpresas(Long pessoaId) {
        String sql = "DELETE FROM empresa_pessoa WHERE fk_pessoa = ?";
        jdbcTemplate.update(sql, pessoaId);
    }

    @Transactional
    public void deleteById(Long id) {
        removerTodosVinculosComEmpresas(id);
        String sql = "DELETE FROM pessoa WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}