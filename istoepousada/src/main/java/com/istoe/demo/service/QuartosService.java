package com.istoe.demo.service;

import com.istoe.demo.enums.RoomStatusEnum;
import com.istoe.demo.repository.QuartosRepository;
import com.istoe.demo.request.CreateQuartoRequest;
import com.istoe.demo.request.UpdateQuartoRequest;
import com.istoe.demo.response.CategoriaResponse;
import com.istoe.demo.response.ObjetoResponse;
import com.istoe.demo.response.RoomsResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.List;

@Service
public class QuartosService {

    private final QuartosRepository quartosRepository;

    public QuartosService(QuartosRepository quartosRepository) {
        this.quartosRepository = quartosRepository;
    }

    public RoomsResponse findRoomsByFilters(LocalDate date, RoomStatusEnum status, String searchTerm) {
        LocalDate searchDate = date != null ? date : LocalDate.now();

        return quartosRepository.findRoomsByFilters(searchDate, status, searchTerm);
    }

    public RoomStatusEnum[] getAllRoomStatuses() {
        return RoomStatusEnum.values();
    }

    public List<CategoriaResponse> listarCategorias() {
        return quartosRepository.listarCategorias();
    }

    public Long criarQuarto(CreateQuartoRequest req) {
        validarReq(req.descricao(), req.quantidadePessoas(), req.statusCodigo(), req.categoriaId());
        // valida enum
        RoomStatusEnum.fromCodigo(req.statusCodigo()); // lança IllegalArgument se inválido
        return quartosRepository.inserirQuarto(req);
    }

    public void atualizarQuarto(Long id, UpdateQuartoRequest req) {
        Assert.notNull(id, "id é obrigatório");
        validarReq(req.descricao(), req.quantidadePessoas(), req.statusCodigo(), req.categoriaId());
        RoomStatusEnum.fromCodigo(req.statusCodigo());
        int rows = quartosRepository.atualizarQuarto(id, req);
        Assert.isTrue(rows > 0, "Quarto não encontrado para atualização");
    }

    private void validarReq(String descricao, Integer quantidade, Integer statusCodigo, Long categoriaId) {
        Assert.hasText(descricao, "descricao é obrigatória");
        Assert.notNull(quantidade, "quantidadePessoas é obrigatória");
        Assert.notNull(statusCodigo, "statusCodigo é obrigatório");
        Assert.notNull(categoriaId, "categoriaId é obrigatório");
    }

    public List<ObjetoResponse> listarQuartosEnum() {
        return quartosRepository.listarQuartosEnum();
    }
}