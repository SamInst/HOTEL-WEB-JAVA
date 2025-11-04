package com.istoe.demo.controller;

import com.istoe.demo.response.Empresa;
import com.istoe.demo.service.EmpresaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public ResponseEntity<List<Empresa>> listarTodas() {
        return ResponseEntity.ok(empresaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> buscarPorId(@PathVariable Long id) {
        return empresaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar/nome")
    public ResponseEntity<List<Empresa>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(empresaService.buscarPorNome(nome));
    }

    @GetMapping("/buscar/cnpj/{cnpj}")
    public ResponseEntity<Empresa> buscarPorCnpj(@PathVariable String cnpj) {
        return empresaService.buscarPorCnpj(cnpj)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Empresa>> buscarPorNomeOuCnpj(@RequestParam String termo) {
        return ResponseEntity.ok(empresaService.buscarPorNomeOuCnpj(termo));
    }

    @PostMapping
    public ResponseEntity<Empresa> cadastrar(@RequestBody Empresa empresa) {
        Empresa novaEmpresa = empresaService.cadastrar(empresa);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaEmpresa);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> atualizar(@PathVariable Long id, @RequestBody Empresa empresa) {
        if (!id.equals(empresa.id())) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Empresa empresaAtualizada = empresaService.atualizar(empresa);
            return ResponseEntity.ok(empresaAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{empresaId}/vincular-pessoa/{pessoaId}")
    public ResponseEntity<Void> vincularPessoa(
            @PathVariable Long empresaId,
            @PathVariable Long pessoaId) {
        empresaService.vincularPessoa(empresaId, pessoaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{empresaId}/vincular-pessoas")
    public ResponseEntity<Void> vincularPessoas(
            @PathVariable Long empresaId,
            @RequestBody List<Long> pessoaIds) {
        empresaService.vincularPessoas(empresaId, pessoaIds);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{empresaId}/desvincular-pessoa/{pessoaId}")
    public ResponseEntity<Void> desvincularPessoa(
            @PathVariable Long empresaId,
            @PathVariable Long pessoaId) {
        empresaService.desvincularPessoa(empresaId, pessoaId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{empresaId}/desvincular-todas-pessoas")
    public ResponseEntity<Void> desvincularTodasPessoas(@PathVariable Long empresaId) {
        empresaService.desvincularTodasPessoas(empresaId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        empresaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
