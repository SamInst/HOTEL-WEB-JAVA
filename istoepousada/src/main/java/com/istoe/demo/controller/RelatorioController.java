package com.istoe.demo.controller;

import com.istoe.demo.request.RelatorioRequest;
import com.istoe.demo.response.RelatorioResponse;
import com.istoe.demo.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@CrossOrigin(origins = "*")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @PostMapping
    public ResponseEntity<RelatorioResponse> create(
            @RequestBody RelatorioRequest request) {
        RelatorioResponse response = relatorioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RelatorioResponse> update(
            @PathVariable Long id,
            @RequestBody RelatorioRequest request) {
        RelatorioResponse response = relatorioService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RelatorioResponse> findById(
            @PathVariable Long id) {
        RelatorioResponse response = relatorioService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RelatorioResponse>> findByFilters(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,

            @RequestParam(required = false) Long tipoPagamentoId,

            @RequestParam(required = false) Long quartoId,

            @RequestParam(required = false) Long pernoiteId) {

        List<RelatorioResponse> response = relatorioService.findByFilters(dataInicio, dataFim, tipoPagamentoId, quartoId, pernoiteId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        relatorioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}