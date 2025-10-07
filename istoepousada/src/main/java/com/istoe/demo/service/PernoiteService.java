package com.istoe.demo.service;

import com.istoe.demo.enums.StatusPernoiteEnum;
import com.istoe.demo.repository.PernoiteRepository;
import com.istoe.demo.request.CreatePernoiteRequest;
import com.istoe.demo.request.HospedeRequest;
import com.istoe.demo.request.PagamentoRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PernoiteService {

    private final PernoiteRepository pernoiteRepository;

    public PernoiteService(PernoiteRepository pernoiteRepository) {
        this.pernoiteRepository = pernoiteRepository;
    }

    @Transactional
    public Long criarPernoite(CreatePernoiteRequest request) {
        if (!Objects.equals(request.dataEntrada(), LocalDate.now())) {
            throw new IllegalArgumentException("Data de entrada não pode ser diferente de hoje");
        }

        if (request.quarto() == null)
            throw new IllegalArgumentException("O quarto é obrigatório.");

        if (request.dataSaida() == null)
            throw new IllegalArgumentException("As datas de entrada e saída são obrigatórias.");

        if (request.dataSaida().isBefore(request.dataEntrada()))
            throw new IllegalArgumentException("A data de saída não pode ser anterior à data de entrada.");

        return pernoiteRepository.adicionarPernoite(request);
    }

    @Transactional
    public void adicionarDiarias(Long pernoiteId, LocalDate novaDataInicio, LocalDate novaDataFim,
                                 Long quartoId, List<HospedeRequest> hospedes, List<PagamentoRequest> pagamentos) {

        if (pernoiteId == null)
            throw new IllegalArgumentException("O ID do pernoite é obrigatório.");
        if (novaDataInicio == null || novaDataFim == null)
            throw new IllegalArgumentException("As datas de início e fim são obrigatórias.");
        if (novaDataFim.isBefore(novaDataInicio))
            throw new IllegalArgumentException("A nova data final não pode ser anterior à inicial.");
        if (quartoId == null)
            throw new IllegalArgumentException("O ID do quarto é obrigatório.");

        long totalRepresentantes = hospedes == null ? 0 :
                hospedes.stream().filter(HospedeRequest::representante).count();

        if (totalRepresentantes > 1)
            throw new IllegalArgumentException("Apenas um hóspede pode ser o representante do pernoite.");

        pernoiteRepository.adicionarDiariasAoPernoite(
                pernoiteId,
                novaDataInicio,
                novaDataFim,
                quartoId,
                hospedes,
                pagamentos
        );
    }


    @Transactional
    public void cancelarPernoite(Long pernoiteId, String motivo) {
        if (pernoiteId == null)
            throw new IllegalArgumentException("O ID do pernoite é obrigatório para cancelamento.");

        pernoiteRepository.cancelarPernoite(pernoiteId, motivo);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPorStatus(StatusPernoiteEnum status) {
        return pernoiteRepository.listarPorStatus(status);
    }
}
