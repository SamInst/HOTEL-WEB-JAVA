package com.istoe.demo.controller;

import com.istoe.demo.response.Empresa;
import com.istoe.demo.response.Pessoa;
import com.istoe.demo.service.VinculoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vinculos")
public class VinculoController {

    private final VinculoService vinculoService;

    public VinculoController(VinculoService vinculoService) {
        this.vinculoService = vinculoService;
    }

    @GetMapping("/pessoa/{pessoaId}")
    public ResponseEntity<Pessoa> buscarPessoaComEmpresas(@PathVariable Long pessoaId) {
        return vinculoService.buscarPessoaComEmpresas(pessoaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<Empresa> buscarEmpresaComPessoas(@PathVariable Long empresaId) {
        return vinculoService.buscarEmpresaComPessoas(empresaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pessoas")
    public ResponseEntity<List<Pessoa>> listarTodasPessoasComEmpresas() {
        return ResponseEntity.ok(vinculoService.listarTodasPessoasComEmpresas());
    }

    @GetMapping("/empresas")
    public ResponseEntity<List<Empresa>> listarTodasEmpresasComPessoas() {
        return ResponseEntity.ok(vinculoService.listarTodasEmpresasComPessoas());
    }

    @PostMapping("/criar")
    public ResponseEntity<Void> criarVinculo(
            @RequestParam Long empresaId,
            @RequestParam Long pessoaId) {
        try {
            vinculoService.criarVinculo(empresaId, pessoaId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/criar-multiplos")
    public ResponseEntity<Void> criarVinculos(
            @RequestParam Long empresaId,
            @RequestBody List<Long> pessoaIds) {
        try {
            vinculoService.criarVinculos(empresaId, pessoaIds);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/remover")
    public ResponseEntity<Void> removerVinculo(
            @RequestParam Long empresaId,
            @RequestParam Long pessoaId) {
        vinculoService.removerVinculo(empresaId, pessoaId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/empresa/{empresaId}")
    public ResponseEntity<Void> removerTodosVinculosEmpresa(@PathVariable Long empresaId) {
        vinculoService.removerTodosVinculosEmpresa(empresaId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/pessoa/{pessoaId}")
    public ResponseEntity<Void> removerTodosVinculosPessoa(@PathVariable Long pessoaId) {
        vinculoService.removerTodosVinculosPessoa(pessoaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verificar")
    public ResponseEntity<Boolean> verificarVinculo(
            @RequestParam Long empresaId,
            @RequestParam Long pessoaId) {
        return ResponseEntity.ok(vinculoService.verificarVinculo(empresaId, pessoaId));
    }

    @GetMapping("/pessoa/{pessoaId}/empresas")
    public ResponseEntity<List<Empresa>> buscarEmpresasDaPessoa(@PathVariable Long pessoaId) {
        return ResponseEntity.ok(vinculoService.buscarEmpresasDaPessoa(pessoaId));
    }

//    @GetMapping("/empresa/{empresaId}/pessoas")
//    public ResponseEntity<List<Pessoa>> buscarPessoasDaEmpresa(@PathVariable Long empresaId) {
//        return ResponseEntity.ok(vinculoService.buscarPessoasDaEmpresa(empresaId));
//    }
}
