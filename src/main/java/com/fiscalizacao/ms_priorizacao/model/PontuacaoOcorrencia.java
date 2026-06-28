package com.fiscalizacao.ms_priorizacao.model;

import com.fiscalizacao.ms_priorizacao.enums.NivelPrioridade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "pontuacao_ocorrencia")
@Schema(description = "Registro histórico de pontuação calculada para uma ocorrência")
public class PontuacaoOcorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID interno do registro de pontuação", example = "1")
    private Long id;

    @Schema(description = "ID da ocorrência referenciada no sistema de origem", example = "42")
    private Long ocorrenciaId;

    @Schema(description = "Pontuação total calculada (0–80)", example = "55")
    private Integer scoreCalculado;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Nível de prioridade resultante do score", example = "ALTA")
    private NivelPrioridade nivelPrioridade;

    @Schema(description = "Data e hora em que o cálculo foi realizado", example = "2026-06-28T14:00:00")
    private LocalDateTime calculadoEm;
}
