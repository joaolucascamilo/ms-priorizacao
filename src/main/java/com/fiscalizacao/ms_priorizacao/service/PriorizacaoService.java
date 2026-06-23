package com.fiscalizacao.ms_priorizacao.service;

import com.fiscalizacao.ms_priorizacao.dto.OcorrenciaPriorizacaoDTO;
import com.fiscalizacao.ms_priorizacao.dto.PrioridadeResponseDTO;
import com.fiscalizacao.ms_priorizacao.enums.NivelPrioridade;
import com.fiscalizacao.ms_priorizacao.model.PontuacaoOcorrencia;
import com.fiscalizacao.ms_priorizacao.repository.PontuacaoOcorrenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriorizacaoService {

    private final PontuacaoOcorrenciaRepository repository;

    public PrioridadeResponseDTO calcular(OcorrenciaPriorizacaoDTO dto) {
        // Só calcula o score, sem salvar — reutilizável pelo recalcular
        int score = 0;
        StringBuilder justificativa = new StringBuilder();

        int pontosTipo = calcularPontosPorTipo(dto.getTipoOcorrencia());
        score += pontosTipo;
        justificativa.append("Tipo '").append(dto.getTipoOcorrencia())
                .append("': +").append(pontosTipo).append("pts. ");

        int pontosDenuncias = calcularPontosPorDenuncias(dto.getQuantidadeDenuncias());
        score += pontosDenuncias;
        justificativa.append("Denúncias (").append(dto.getQuantidadeDenuncias())
                .append("): +").append(pontosDenuncias).append("pts. ");

        int pontosTempo = calcularPontosPorTempo(dto.getDataCriacao());
        score += pontosTempo;
        justificativa.append("Tempo sem solução: +").append(pontosTempo).append("pts. ");

        NivelPrioridade nivel = NivelPrioridade.fromScore(score);

        // Salva novo registro (primeira vez)
        PontuacaoOcorrencia pontuacao = new PontuacaoOcorrencia();
        pontuacao.setOcorrenciaId(dto.getId());
        pontuacao.setScoreCalculado(score);
        pontuacao.setNivelPrioridade(nivel);
        pontuacao.setCalculadoEm(LocalDateTime.now());
        repository.save(pontuacao);

        return new PrioridadeResponseDTO(dto.getId(), nivel, score, justificativa.toString());
    }

    public PrioridadeResponseDTO recalcular(Long ocorrenciaId, OcorrenciaPriorizacaoDTO dto) {

        // Busca o registro existente para ATUALIZAR (não criar novo)
        PontuacaoOcorrencia pontuacao = repository
                .findTopByOcorrenciaIdOrderByCalculadoEmDesc(ocorrenciaId)
                .orElse(new PontuacaoOcorrencia()); // se não existir, cria

        // Recalcula o score
        int score = 0;
        StringBuilder justificativa = new StringBuilder();

        int pontosTipo = calcularPontosPorTipo(dto.getTipoOcorrencia());
        score += pontosTipo;
        justificativa.append("Tipo '").append(dto.getTipoOcorrencia())
                .append("': +").append(pontosTipo).append("pts. ");

        int pontosDenuncias = calcularPontosPorDenuncias(dto.getQuantidadeDenuncias());
        score += pontosDenuncias;
        justificativa.append("Denúncias (").append(dto.getQuantidadeDenuncias())
                .append("): +").append(pontosDenuncias).append("pts. ");

        int pontosTempo = calcularPontosPorTempo(dto.getDataCriacao());
        score += pontosTempo;
        justificativa.append("Tempo sem solução: +").append(pontosTempo).append("pts. ");

        NivelPrioridade novoNivel = NivelPrioridade.fromScore(score);

        // Loga escalada de nível
        if (pontuacao.getNivelPrioridade() != null && pontuacao.getNivelPrioridade() != novoNivel) {
            System.out.printf("Ocorrência %d escalou de %s para %s (score: %d → %d)%n",
                    ocorrenciaId,
                    pontuacao.getNivelPrioridade(),
                    novoNivel,
                    pontuacao.getScoreCalculado(),
                    score
            );
        }

        // Atualiza o registro existente em vez de criar um novo
        pontuacao.setOcorrenciaId(ocorrenciaId);
        pontuacao.setScoreCalculado(score);
        pontuacao.setNivelPrioridade(novoNivel);
        pontuacao.setCalculadoEm(LocalDateTime.now());
        repository.save(pontuacao);

        return new PrioridadeResponseDTO(ocorrenciaId, novoNivel, score, justificativa.toString());
    }

    public List<PontuacaoOcorrencia> gerarRanking(NivelPrioridade nivel) {
        if (nivel != null) {
            return repository.findByNivelPrioridadeOrderByScoreCalculadoDescCalculadoEmAsc(nivel);
        }
        return repository.findAllByOrderByScoreCalculadoDescCalculadoEmAsc();
    }

    private int calcularPontosPorTipo(String tipo) {
        return switch (tipo.toUpperCase()) {
            case "ESGOTO"   -> 40;  // risco de saúde pública
            case "BURACO"   -> 35;  // risco de acidente
            case "SEMAFORO" -> 30;  // risco de trânsito
            case "LAMPADA"  -> 15;  // risco de segurança
            default         -> 10;
        };
    }

    private int calcularPontosPorDenuncias(Integer quantidade) {
        if (quantidade == null) return 0;
        if (quantidade >= 10) return 20;
        if (quantidade >= 5)  return 10;
        if (quantidade >= 2)  return 5;
        return 0;
    }

    private int calcularPontosPorTempo(LocalDateTime dataCriacao) {
        if (dataCriacao == null) return 0;
        long dias = ChronoUnit.DAYS.between(dataCriacao, LocalDateTime.now());
        if (dias >= 30) return 20;
        if (dias >= 14) return 10;
        if (dias >= 7)  return 5;
        return 0;
    }

}
