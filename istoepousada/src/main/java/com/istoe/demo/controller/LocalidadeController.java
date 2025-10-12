package com.istoe.demo.controller;

import com.istoe.demo.response.LocalidadeResponse;
import com.istoe.demo.service.EnderecoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/localidades")
@CrossOrigin(origins = "*") // 🔓 permite acesso do front (React, etc.)
public class LocalidadeController {

    private final EnderecoService enderecoService;

    public LocalidadeController(EnderecoService enderecoService) {
        this.enderecoService = enderecoService;
    }

    /**
     * Retorna todos os países cadastrados.
     */
    @GetMapping("/paises")
    public ResponseEntity<List<LocalidadeResponse>> listarPaises() {
        List<LocalidadeResponse> response = enderecoService.listarPaises();
        return ResponseEntity.ok(response);
    }

    /**
     * Retorna todos os estados de um país.
     *
     * Exemplo: GET /localidades/estados/1
     */
    @GetMapping("/estados/{fkPais}")
    public ResponseEntity<List<LocalidadeResponse>> listarEstadosPorPais(@PathVariable Long fkPais) {
        List<LocalidadeResponse> response = enderecoService.listarEstados(fkPais);
        return ResponseEntity.ok(response);
    }

    /**
     * Retorna todos os municípios de um estado.
     *
     * Exemplo: GET /localidades/municipios/10
     */
    @GetMapping("/municipios/{fkEstado}")
    public ResponseEntity<List<LocalidadeResponse>> listarMunicipiosPorEstado(@PathVariable Long fkEstado) {
        List<LocalidadeResponse> response = enderecoService.listarMunicipios(fkEstado);
        return ResponseEntity.ok(response);
    }
}
