package com.istoe.demo.controller;

import com.istoe.demo.enums.StatusPernoiteEnum;
import com.istoe.demo.request.CreatePernoiteRequest;
import com.istoe.demo.request.UpdateDiariaRequest;
import com.istoe.demo.service.PernoiteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pernoite")
public class PernoiteController {

    private final PernoiteService pernoiteService;

    public PernoiteController(PernoiteService pernoiteService) {
        this.pernoiteService = pernoiteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> criarPernoite(@RequestBody CreatePernoiteRequest request) {
        Long id = pernoiteService.criarPernoite(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PutMapping("/{pernoiteId}/diarias")
    @ResponseStatus(HttpStatus.OK)
    public void adicionarDiariasAoPernoite(
            @PathVariable Long pernoiteId,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate novaDataInicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate novaDataFim,
            @RequestBody UpdateDiariaRequest request
    ) {
        pernoiteService.adicionarDiarias(
                pernoiteId,
                novaDataInicio,
                novaDataFim,
                request.quarto(),
                request.hospedes(),
                request.pagamentoRequestList()
        );
    }


    @DeleteMapping("/{pernoiteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelarPernoite(
            @PathVariable Long pernoiteId,
            @RequestParam(value = "motivo", required = false) String motivo
    ) {
        pernoiteService.cancelarPernoite(pernoiteId, motivo);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Map<String, Object>>> listarPorStatus(
            @RequestParam(value = "status", required = false) StatusPernoiteEnum status
    ) {
        List<Map<String, Object>> lista = pernoiteService.listarPorStatus(status);
        return ResponseEntity.ok(lista);
    }
}
