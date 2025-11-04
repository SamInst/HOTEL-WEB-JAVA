package com.istoe.demo.repository;

import com.istoe.demo.response.Empresa;
import com.istoe.demo.response.Pessoa;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Repository
public class EmpresaRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmpresaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final ResultSetExtractor<List<Empresa>> EMPRESA_COM_PESSOAS_EXTRACTOR = rs -> {
        Map<Long, Empresa> empresaMap = new LinkedHashMap<>();
        Map<Long, List<Pessoa>> pessoasPorEmpresa = new HashMap<>();

        while (rs.next()) {
            Long empresaId = rs.getLong("id");

            if (!empresaMap.containsKey(empresaId)) {
                Empresa empresa = mapEmpresa(rs);
                empresaMap.put(empresaId, empresa);
                pessoasPorEmpresa.put(empresaId, new ArrayList<>());
            }

            Long pessoaId = rs.getObject("pessoa_id", Long.class);
            if (pessoaId != null) {
                Pessoa pessoa = mapPessoa(rs, "pessoa_");
                pessoasPorEmpresa.get(empresaId).add(pessoa);
            }
        }

        return empresaMap.entrySet().stream()
                .map(entry -> entry.getValue().withPessoas(pessoasPorEmpresa.get(entry.getKey())))
                .toList();
    };

    private Empresa mapEmpresa(ResultSet rs) throws SQLException {
        return new Empresa(
                rs.getLong("id"),
                rs.getString("razao_social"),
                rs.getString("nome_fantasia"),
                rs.getString("cnpj"),
                rs.getString("inscricao_estadual"),
                rs.getString("inscricao_municipal"),
                rs.getString("telefone"),
                rs.getString("email"),
                rs.getString("endereco"),
                rs.getString("cep"),
                rs.getString("numero"),
                rs.getString("complemento"),
                rs.getObject("fk_pais", Long.class),
                rs.getObject("fk_estado", Long.class),
                rs.getObject("fk_municipio", Long.class),
                rs.getString("bairro"),
                rs.getString("tipo_empresa"),
                rs.getBoolean("ativa"),
                List.of()
        );
    }

    private Pessoa mapPessoa(ResultSet rs, String prefix) throws SQLException {
        return new Pessoa(
                rs.getLong(prefix + "id"),
                rs.getTimestamp(prefix + "data_hora_cadastro") != null ?
                        rs.getTimestamp(prefix + "data_hora_cadastro").toLocalDateTime() : null,
                rs.getString(prefix + "nome"),
                rs.getDate(prefix + "data_nascimento") != null ?
                        rs.getDate(prefix + "data_nascimento").toLocalDate() : null,
                rs.getString(prefix + "cpf"),
                rs.getString(prefix + "rg"),
                rs.getString(prefix + "email"),
                rs.getString(prefix + "telefone"),
                rs.getObject(prefix + "fk_pais", Long.class),
                rs.getObject(prefix + "fk_estado", Long.class),
                rs.getObject(prefix + "fk_municipio", Long.class),
                rs.getString(prefix + "endereco"),
                rs.getString(prefix + "complemento"),
                rs.getObject(prefix + "hospedado", Boolean.class),
                rs.getObject(prefix + "vezes_hospedado", Integer.class),
                rs.getObject(prefix + "cliente_novo", Boolean.class),
                rs.getString(prefix + "cep"),
                rs.getObject(prefix + "idade", Integer.class),
                rs.getString(prefix + "bairro"),
                rs.getObject(prefix + "sexo", Short.class),
                rs.getString(prefix + "numero"),
                List.of()
        );
    }

    public Optional<Empresa> findById(Long id) {
        String sql = """
            SELECT
                e.*,
                p.id as pessoa_id,
                p.data_hora_cadastro as pessoa_data_hora_cadastro,
                p.nome as pessoa_nome,
                p.data_nascimento as pessoa_data_nascimento,
                p.cpf as pessoa_cpf,
                p.rg as pessoa_rg,
                p.email as pessoa_email,
                p.telefone as pessoa_telefone,
                p.fk_pais as pessoa_fk_pais,
                p.fk_estado as pessoa_fk_estado,
                p.fk_municipio as pessoa_fk_municipio,
                p.endereco as pessoa_endereco,
                p.complemento as pessoa_complemento,
                p.hospedado as pessoa_hospedado,
                p.vezes_hospedado as pessoa_vezes_hospedado,
                p.cliente_novo as pessoa_cliente_novo,
                p.cep as pessoa_cep,
                p.idade as pessoa_idade,
                p.bairro as pessoa_bairro,
                p.sexo as pessoa_sexo,
                p.numero as pessoa_numero
            FROM empresa e
            LEFT JOIN empresa_pessoa ep ON e.id = ep.fk_empresa
            LEFT JOIN pessoa p ON ep.fk_pessoa = p.id
            WHERE e.id = ?
            ORDER BY p.nome
        """;

        List<Empresa> empresas = jdbcTemplate.query(sql, EMPRESA_COM_PESSOAS_EXTRACTOR, id);
        return empresas.isEmpty() ? Optional.empty() : Optional.of(empresas.get(0));
    }

    public List<Empresa> findByNome(String nome) {
        String sql = """
            SELECT 
                e.*,
                p.id as pessoa_id,
                p.data_hora_cadastro as pessoa_data_hora_cadastro,
                p.nome as pessoa_nome,
                p.data_nascimento as pessoa_data_nascimento,
                p.cpf as pessoa_cpf,
                p.rg as pessoa_rg,
                p.email as pessoa_email,
                p.telefone as pessoa_telefone,
                p.fk_pais as pessoa_fk_pais,
                p.fk_estado as pessoa_fk_estado,
                p.fk_municipio as pessoa_fk_municipio,
                p.endereco as pessoa_endereco,
                p.complemento as pessoa_complemento,
                p.hospedado as pessoa_hospedado,
                p.vezes_hospedado as pessoa_vezes_hospedado,
                p.cliente_novo as pessoa_cliente_novo,
                p.cep as pessoa_cep,
                p.idade as pessoa_idade,
                p.bairro as pessoa_bairro,
                p.sexo as pessoa_sexo,
                p.numero as pessoa_numero
            FROM empresa e
            LEFT JOIN empresa_pessoa ep ON e.id = ep.fk_empresa
            LEFT JOIN pessoa p ON ep.fk_pessoa = p.id
            WHERE e.razao_social ILIKE ? OR e.nome_fantasia ILIKE ?
            ORDER BY e.razao_social, p.nome
        """;

        String search = "%" + nome + "%";
        return jdbcTemplate.query(sql, EMPRESA_COM_PESSOAS_EXTRACTOR, search, search);
    }

    public Optional<Empresa> findByCnpj(String cnpj) {
        String sql = """
            SELECT 
                e.*,
                p.id as pessoa_id,
                p.data_hora_cadastro as pessoa_data_hora_cadastro,
                p.nome as pessoa_nome,
                p.data_nascimento as pessoa_data_nascimento,
                p.cpf as pessoa_cpf,
                p.rg as pessoa_rg,
                p.email as pessoa_email,
                p.telefone as pessoa_telefone,
                p.fk_pais as pessoa_fk_pais,
                p.fk_estado as pessoa_fk_estado,
                p.fk_municipio as pessoa_fk_municipio,
                p.endereco as pessoa_endereco,
                p.complemento as pessoa_complemento,
                p.hospedado as pessoa_hospedado,
                p.vezes_hospedado as pessoa_vezes_hospedado,
                p.cliente_novo as pessoa_cliente_novo,
                p.cep as pessoa_cep,
                p.idade as pessoa_idade,
                p.bairro as pessoa_bairro,
                p.sexo as pessoa_sexo,
                p.numero as pessoa_numero
            FROM empresa e
            LEFT JOIN empresa_pessoa ep ON e.id = ep.fk_empresa
            LEFT JOIN pessoa p ON ep.fk_pessoa = p.id
            WHERE e.cnpj = ?
            ORDER BY p.nome
        """;

        List<Empresa> empresas = jdbcTemplate.query(sql, EMPRESA_COM_PESSOAS_EXTRACTOR, cnpj);
        return empresas.isEmpty() ? Optional.empty() : Optional.of(empresas.get(0));
    }

    public List<Empresa> findByNomeOrCnpj(String termo) {
        String sql = """
            SELECT 
                e.*,
                p.id as pessoa_id,
                p.data_hora_cadastro as pessoa_data_hora_cadastro,
                p.nome as pessoa_nome,
                p.data_nascimento as pessoa_data_nascimento,
                p.cpf as pessoa_cpf,
                p.rg as pessoa_rg,
                p.email as pessoa_email,
                p.telefone as pessoa_telefone,
                p.fk_pais as pessoa_fk_pais,
                p.fk_estado as pessoa_fk_estado,
                p.fk_municipio as pessoa_fk_municipio,
                p.endereco as pessoa_endereco,
                p.complemento as pessoa_complemento,
                p.hospedado as pessoa_hospedado,
                p.vezes_hospedado as pessoa_vezes_hospedado,
                p.cliente_novo as pessoa_cliente_novo,
                p.cep as pessoa_cep,
                p.idade as pessoa_idade,
                p.bairro as pessoa_bairro,
                p.sexo as pessoa_sexo,
                p.numero as pessoa_numero
            FROM empresa e
            LEFT JOIN empresa_pessoa ep ON e.id = ep.fk_empresa
            LEFT JOIN pessoa p ON ep.fk_pessoa = p.id
            WHERE e.razao_social ILIKE ? OR e.nome_fantasia ILIKE ? OR e.cnpj = ?
            ORDER BY e.razao_social, p.nome
        """;

        String search = "%" + termo + "%";
        return jdbcTemplate.query(sql, EMPRESA_COM_PESSOAS_EXTRACTOR, search, search, termo);
    }

    public List<Empresa> findAll() {
        String sql = """
            SELECT 
                e.*,
                p.id as pessoa_id,
                p.data_hora_cadastro as pessoa_data_hora_cadastro,
                p.nome as pessoa_nome,
                p.data_nascimento as pessoa_data_nascimento,
                p.cpf as pessoa_cpf,
                p.rg as pessoa_rg,
                p.email as pessoa_email,
                p.telefone as pessoa_telefone,
                p.fk_pais as pessoa_fk_pais,
                p.fk_estado as pessoa_fk_estado,
                p.fk_municipio as pessoa_fk_municipio,
                p.endereco as pessoa_endereco,
                p.complemento as pessoa_complemento,
                p.hospedado as pessoa_hospedado,
                p.vezes_hospedado as pessoa_vezes_hospedado,
                p.cliente_novo as pessoa_cliente_novo,
                p.cep as pessoa_cep,
                p.idade as pessoa_idade,
                p.bairro as pessoa_bairro,
                p.sexo as pessoa_sexo,
                p.numero as pessoa_numero
            FROM empresa e
            LEFT JOIN empresa_pessoa ep ON e.id = ep.fk_empresa
            LEFT JOIN pessoa p ON ep.fk_pessoa = p.id
            ORDER BY e.razao_social, p.nome
        """;

        return jdbcTemplate.query(sql, EMPRESA_COM_PESSOAS_EXTRACTOR);
    }

    @Transactional
    public Empresa save(Empresa empresa) {
        if (empresa.id() == null) {
            return insert(empresa);
        } else {
            update(empresa);
            return findById(empresa.id()).orElse(empresa);
        }
    }

    private Empresa insert(Empresa empresa) {
        String sql = """
            INSERT INTO empresa (
                razao_social, nome_fantasia, cnpj, inscricao_estadual, inscricao_municipal,
                telefone, email, endereco, cep, numero, complemento, 
                fk_pais, fk_estado, fk_municipio, bairro, tipo_empresa, ativa
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int idx = 1;
            ps.setString(idx++, empresa.razaoSocial());
            ps.setString(idx++, empresa.nomeFantasia());
            ps.setString(idx++, empresa.cnpj());
            ps.setString(idx++, empresa.inscricaoEstadual());
            ps.setString(idx++, empresa.inscricaoMunicipal());
            ps.setString(idx++, empresa.telefone());
            ps.setString(idx++, empresa.email());
            ps.setString(idx++, empresa.endereco());
            ps.setString(idx++, empresa.cep());
            ps.setString(idx++, empresa.numero());
            ps.setString(idx++, empresa.complemento());
            ps.setObject(idx++, empresa.fkPais());
            ps.setObject(idx++, empresa.fkEstado());
            ps.setObject(idx++, empresa.fkMunicipio());
            ps.setString(idx++, empresa.bairro());
            ps.setString(idx++, empresa.tipoEmpresa());
            ps.setBoolean(idx++, empresa.ativa());
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        return findById(generatedId).orElse(empresa.withId(generatedId));
    }

    @Transactional
    public void update(Empresa empresa) {
        String sql = """
            UPDATE empresa SET
                razao_social = ?, nome_fantasia = ?, cnpj = ?, 
                inscricao_estadual = ?, inscricao_municipal = ?,
                telefone = ?, email = ?, endereco = ?, cep = ?, 
                numero = ?, complemento = ?, fk_pais = ?, fk_estado = ?, 
                fk_municipio = ?, bairro = ?, tipo_empresa = ?, ativa = ?
            WHERE id = ?
        """;

        jdbcTemplate.update(sql,
                empresa.razaoSocial(),
                empresa.nomeFantasia(),
                empresa.cnpj(),
                empresa.inscricaoEstadual(),
                empresa.inscricaoMunicipal(),
                empresa.telefone(),
                empresa.email(),
                empresa.endereco(),
                empresa.cep(),
                empresa.numero(),
                empresa.complemento(),
                empresa.fkPais(),
                empresa.fkEstado(),
                empresa.fkMunicipio(),
                empresa.bairro(),
                empresa.tipoEmpresa(),
                empresa.ativa(),
                empresa.id()
        );
    }

    @Transactional
    public void vincularPessoas(Long empresaId, List<Long> pessoaIds) {
        String sql = "INSERT INTO empresa_pessoa (fk_empresa, fk_pessoa) VALUES (?, ?) ON CONFLICT DO NOTHING";

        List<Object[]> batchArgs = pessoaIds.stream()
                .map(pessoaId -> new Object[]{empresaId, pessoaId})
                .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Transactional
    public void desvincularPessoa(Long empresaId, Long pessoaId) {
        String sql = "DELETE FROM empresa_pessoa WHERE fk_empresa = ? AND fk_pessoa = ?";
        jdbcTemplate.update(sql, empresaId, pessoaId);
    }

    @Transactional
    public void desvincularTodasPessoas(Long empresaId) {
        String sql = "DELETE FROM empresa_pessoa WHERE fk_empresa = ?";
        jdbcTemplate.update(sql, empresaId);
    }

    @Transactional
    public void deleteById(Long id) {
        desvincularTodasPessoas(id);
        String sql = "DELETE FROM empresa WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}