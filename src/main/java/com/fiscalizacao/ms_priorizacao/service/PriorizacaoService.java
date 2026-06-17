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
import java.util.Optional;

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

        int pontosProximidade = calcularPontosPorProximidade(dto.getLatitude(), dto.getLongitude());
        score += pontosProximidade;
        justificativa.append("Proximidade de escola/hospital: +").append(pontosProximidade).append("pts.");

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

        int pontosProximidade = calcularPontosPorProximidade(dto.getLatitude(), dto.getLongitude());
        score += pontosProximidade;
        justificativa.append("Proximidade de escola/hospital: +").append(pontosProximidade).append("pts.");

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

    private int calcularPontosPorProximidade(Double lat, Double lng) {
        if (lat == null || lng == null) return 0;

        // Lista de locais críticos (escolas/hospitais) — em produção viria de um banco ou API
        // Para o TCC, usamos coordenadas fixas de Fortaleza como exemplo
        List<double[]> locaisCriticos = List.of(
                new double[]{-3.7418, -38.5267},  // Hospital Geral de Fortaleza
                new double[]{-3.7234, -38.5432},  // UFC Campus do Pici
                new double[]{-3.7318, -38.5001}   // Escola Estadual exemplo
        );

        double menorDistancia = locaisCriticos.stream()
                .mapToDouble(local -> calcularDistanciaKm(lat, lng, local[0], local[1]))
                .min()
                .orElse(Double.MAX_VALUE);

        if (menorDistancia <= 0.2) return 20;  // até 200m
        if (menorDistancia <= 0.5) return 10;  // até 500m
        if (menorDistancia <= 1.0) return 5;   // até 1km
        return 0;
    }

    // Fórmula de Haversine — calcula distância entre dois pontos geográficos
    private double calcularDistanciaKm(double lat1, double lng1, double lat2, double lng2) {
        final int RAIO_TERRA_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return RAIO_TERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
