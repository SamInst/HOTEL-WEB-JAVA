package com.istoe.demo.controller;

import com.istoe.demo.response.Pessoa;
import com.istoe.demo.service.PessoaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pessoas")
public class PessoaController {

    private final PessoaService pessoaService;

    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    @GetMapping
    public ResponseEntity<List<Pessoa>> listarTodos() {
        return ResponseEntity.ok(pessoaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pessoa> buscarPorId(@PathVariable Long id) {
        return pessoaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar/nome")
    public ResponseEntity<List<Pessoa>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(pessoaService.buscarPorNome(nome));
    }

    @GetMapping("/buscar/cpf/{cpf}")
    public ResponseEntity<Pessoa> buscarPorCpf(@PathVariable String cpf) {
        return pessoaService.buscarPorCpf(cpf)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Pessoa>> buscarPorNomeOuCpf(@RequestParam String termo) {
        return ResponseEntity.ok(pessoaService.buscarPorNomeOuCpf(termo));
    }

    @GetMapping("/hospedados")
    public ResponseEntity<List<Pessoa>> listarHospedados() {
        return ResponseEntity.ok(pessoaService.listarHospedados());
    }

    @PostMapping
    public ResponseEntity<Pessoa> cadastrar(@RequestBody Pessoa pessoa) {
        Pessoa novaPessoa = pessoaService.cadastrar(pessoa);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaPessoa);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pessoa> atualizar(@PathVariable Long id, @RequestBody Pessoa pessoa) {
        if (!id.equals(pessoa.id())) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Pessoa pessoaAtualizada = pessoaService.atualizar(pessoa);
            return ResponseEntity.ok(pessoaAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/hospedar")
    public ResponseEntity<Void> marcarComoHospedado(@PathVariable Long id) {
        pessoaService.marcarComoHospedado(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desospedar")
    public ResponseEntity<Void> marcarComoNaoHospedado(@PathVariable Long id) {
        pessoaService.marcarComoNaoHospedado(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/registrar-hospedagem")
    public ResponseEntity<Void> registrarHospedagem(@PathVariable Long id) {
        pessoaService.registrarHospedagem(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/finalizar-hospedagem")
    public ResponseEntity<Void> finalizarHospedagem(@PathVariable Long id) {
        pessoaService.finalizarHospedagem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/possui-vinculo-empresa")
    public ResponseEntity<Boolean> possuiVinculoComEmpresa(@PathVariable Long id) {
        return ResponseEntity.ok(pessoaService.possuiVinculoComEmpresa(id));
    }

    @GetMapping("/{id}/contar-empresas")
    public ResponseEntity<Integer> contarEmpresasVinculadas(@PathVariable Long id) {
        return ResponseEntity.ok(pessoaService.contarEmpresasVinculadas(id));
    }

    @GetMapping("/{pessoaId}/vinculada-empresa/{empresaId}")
    public ResponseEntity<Boolean> isVinculadaComEmpresa(
            @PathVariable Long pessoaId,
            @PathVariable Long empresaId) {
        return ResponseEntity.ok(pessoaService.isVinculadaComEmpresa(pessoaId, empresaId));
    }

    @DeleteMapping("/{id}/vinculos-empresas")
    public ResponseEntity<Void> removerTodosVinculosComEmpresas(@PathVariable Long id) {
        pessoaService.removerTodosVinculosComEmpresas(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pessoaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
