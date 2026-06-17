package com.fiscalizacao.ms_priorizacao.model;

import com.fiscalizacao.ms_priorizacao.enums.NivelPrioridade;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "pontuacao_ocorrencia")
public class PontuacaoOcorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ocorrenciaId;
    private Integer scoreCalculado;

    @Enumerated(EnumType.STRING)
    private NivelPrioridade nivelPrioridade;

    private LocalDateTime calculadoEm;
}
