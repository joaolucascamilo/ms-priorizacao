package com.fiscalizacao.ms_priorizacao.repository;

import com.fiscalizacao.ms_priorizacao.enums.NivelPrioridade;
import com.fiscalizacao.ms_priorizacao.model.PontuacaoOcorrencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PontuacaoOcorrenciaRepository extends JpaRepository<PontuacaoOcorrencia, Long> {

    Optional<PontuacaoOcorrencia> findTopByOcorrenciaIdOrderByCalculadoEmDesc(Long ocorrenciaId);

    // Ranking geral: score DESC, data ASC (mais antiga primeiro no empate)
    List<PontuacaoOcorrencia> findAllByOrderByScoreCalculadoDescCalculadoEmAsc();

    // Ranking por nível: útil para "quais críticas atender primeiro"
    List<PontuacaoOcorrencia> findByNivelPrioridadeOrderByScoreCalculadoDescCalculadoEmAsc(
            NivelPrioridade nivelPrioridade
    );
}
