package com.fiscalizacao.ms_priorizacao.dto;

import com.fiscalizacao.ms_priorizacao.enums.NivelPrioridade;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado do cálculo de prioridade para uma ocorrência")
public class PrioridadeResponseDTO {

    @Schema(description = "ID da ocorrência avaliada", example = "42")
    private Long ocorrenciaId;

    @Schema(description = "Nível de prioridade calculado: CRITICA (≥65), ALTA (40-64), MEDIA (20-39), BAIXA (<20)", example = "ALTA")
    private NivelPrioridade nivelPrioridade;

    @Schema(description = "Pontuação total calculada (0–80)", example = "55")
    private Integer scoreCalculado;

    @Schema(description = "Texto explicativo dos fatores que determinaram o nível de prioridade", example = "Tipo BURACO (35pts) + 7 denúncias (10pts) + 12 dias (5pts) = 50pts → ALTA")
    private String justificativa;
}
