package com.istoe.demo.controller;

import com.istoe.demo.enums.RoomStatusEnum;
import com.istoe.demo.request.CreateQuartoRequest;
import com.istoe.demo.request.UpdateQuartoRequest;
import com.istoe.demo.response.CategoriaResponse;
import com.istoe.demo.response.ObjetoResponse;
import com.istoe.demo.response.RoomsResponse;
import com.istoe.demo.service.QuartosService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/quartos")
public class QuartosController {

    private final QuartosService quartosService;

    public QuartosController(QuartosService quartosService) {
        this.quartosService = quartosService;
    }

    @GetMapping
    public ResponseEntity<RoomsResponse> getRooms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) RoomStatusEnum status,
            @RequestParam(required = false) String search) {

        RoomsResponse response = quartosService.findRoomsByFilters(date, status, search);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<RoomStatusEnum[]> getRoomStatuses() {
        return ResponseEntity.ok(quartosService.getAllRoomStatuses());
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaResponse>> getCategorias() {
        return ResponseEntity.ok(quartosService.listarCategorias());
    }

    @PostMapping
    public ResponseEntity<Void> criarQuarto(@RequestBody CreateQuartoRequest req) {
        Long id = quartosService.criarQuarto(req);
        return ResponseEntity.created(URI.create("/api/quartos/" + id)).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarQuarto(@PathVariable Long id, @RequestBody UpdateQuartoRequest req) {
        quartosService.atualizarQuarto(id, req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/enum")
    public List<ObjetoResponse> listarQuartosEnum() {
        return quartosService.listarQuartosEnum();
    }
}
