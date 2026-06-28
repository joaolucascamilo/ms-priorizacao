package com.fiscalizacao.ms_priorizacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de uma ocorrência urbana enviados para cálculo de prioridade")
public class OcorrenciaPriorizacaoDTO {

    @Schema(description = "ID da ocorrência no sistema de origem", example = "42")
    private Long id;

    @Schema(
            description = "Tipo da ocorrência urbana. Valores reconhecidos: BURACO, LAMPADA, ESGOTO, SEMAFORO",
            example = "BURACO",
            allowableValues = {"BURACO", "LAMPADA", "ESGOTO", "SEMAFORO"}
    )
    private String tipoOcorrencia;

    @Schema(description = "Número total de denúncias registradas para esta ocorrência", example = "7")
    private Integer quantidadeDenuncias;

    @Schema(description = "Data e hora de criação da ocorrência no sistema de origem", example = "2026-05-01T10:30:00")
    private LocalDateTime dataCriacao;

    @Schema(description = "Latitude geográfica da ocorrência", example = "-23.5505")
    private Double latitude;

    @Schema(description = "Longitude geográfica da ocorrência", example = "-46.6333")
    private Double longitude;
}
