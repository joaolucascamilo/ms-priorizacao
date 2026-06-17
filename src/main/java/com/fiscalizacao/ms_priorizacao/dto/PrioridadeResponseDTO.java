package com.fiscalizacao.ms_priorizacao.dto;

import com.fiscalizacao.ms_priorizacao.enums.NivelPrioridade;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrioridadeResponseDTO {

    private Long ocorrenciaId;
    private NivelPrioridade nivelPrioridade;
    private Integer scoreCalculado;
    private String justificativa;     // explica por que recebeu aquela prioridade
}
