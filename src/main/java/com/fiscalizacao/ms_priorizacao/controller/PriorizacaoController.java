package com.fiscalizacao.ms_priorizacao.controller;

import com.fiscalizacao.ms_priorizacao.dto.OcorrenciaPriorizacaoDTO;
import com.fiscalizacao.ms_priorizacao.dto.PrioridadeResponseDTO;
import com.fiscalizacao.ms_priorizacao.enums.NivelPrioridade;
import com.fiscalizacao.ms_priorizacao.model.PontuacaoOcorrencia;
import com.fiscalizacao.ms_priorizacao.service.PriorizacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/priorizacao")
@RequiredArgsConstructor
@Tag(name = "Priorização de Ocorrências", description = "Calcula e ranqueia a prioridade de ocorrências urbanas com base em tipo, quantidade de denúncias e idade")
public class PriorizacaoController {

    private final PriorizacaoService priorizacaoService;

    @Operation(
            summary = "Calcular prioridade de uma ocorrência",
            description = "Recebe os dados de uma ocorrência urbana, aplica o algoritmo de pontuação e persiste o resultado. " +
                    "Use este endpoint ao criar uma nova ocorrência no sistema de origem."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prioridade calculada com sucesso",
                    content = @Content(schema = @Schema(implementation = PrioridadeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados da ocorrência inválidos", content = @Content)
    })
    @PostMapping("/calcular")
    public ResponseEntity<PrioridadeResponseDTO> calcular(
            @RequestBody @Valid OcorrenciaPriorizacaoDTO dto) {
        return ResponseEntity.ok(priorizacaoService.calcular(dto));
    }

    @Operation(
            summary = "Listar ocorrências ranqueadas por prioridade",
            description = "Retorna todas as ocorrências ordenadas por score decrescente. " +
                    "Filtre pelo parâmetro `nivel` para obter apenas um nível de prioridade específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking retornado com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PontuacaoOcorrencia.class))))
    })
    @GetMapping("/ranking")
    public ResponseEntity<List<PontuacaoOcorrencia>> ranking(
            @Parameter(description = "Filtra o ranking por nível de prioridade. Omita para retornar todos os níveis.")
            @RequestParam(required = false) NivelPrioridade nivel) {
        return ResponseEntity.ok(priorizacaoService.gerarRanking(nivel));
    }

    @Operation(
            summary = "Recalcular prioridade de uma ocorrência existente",
            description = "Atualiza o score e o nível de prioridade de uma ocorrência já registrada. " +
                    "Use este endpoint quando os dados da ocorrência (ex.: novas denúncias) forem atualizados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prioridade recalculada com sucesso",
                    content = @Content(schema = @Schema(implementation = PrioridadeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ocorrência não encontrada", content = @Content)
    })
    @PutMapping("/recalcular/{ocorrenciaId}")
    public ResponseEntity<PrioridadeResponseDTO> recalcular(
            @Parameter(description = "ID da ocorrência no sistema de origem", required = true, example = "42")
            @PathVariable Long ocorrenciaId,
            @RequestBody OcorrenciaPriorizacaoDTO dto) {
        return ResponseEntity.ok(priorizacaoService.recalcular(ocorrenciaId, dto));
    }
}
