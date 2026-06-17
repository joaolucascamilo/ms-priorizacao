package com.fiscalizacao.ms_priorizacao.controller;

import com.fiscalizacao.ms_priorizacao.dto.OcorrenciaPriorizacaoDTO;
import com.fiscalizacao.ms_priorizacao.dto.PrioridadeResponseDTO;
import com.fiscalizacao.ms_priorizacao.enums.NivelPrioridade;
import com.fiscalizacao.ms_priorizacao.model.PontuacaoOcorrencia;
import com.fiscalizacao.ms_priorizacao.service.PriorizacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/priorizacao")
@RequiredArgsConstructor
public class PriorizacaoController {

    private final PriorizacaoService priorizacaoService;

    @PostMapping("/calcular")
    public ResponseEntity<PrioridadeResponseDTO> calcular(
            @RequestBody @Valid OcorrenciaPriorizacaoDTO dto) {
        return ResponseEntity.ok(priorizacaoService.calcular(dto));
    }

    // Retorna todas as ocorrências ordenadas por prioridade
    @GetMapping("/ranking")
    public ResponseEntity<List<PontuacaoOcorrencia>> ranking(
            @RequestParam(required = false) NivelPrioridade nivel) {
        return ResponseEntity.ok(priorizacaoService.gerarRanking(nivel));
    }

    @PutMapping("/recalcular/{ocorrenciaId}")
    public ResponseEntity<PrioridadeResponseDTO> recalcular(
            @PathVariable Long ocorrenciaId,
            @RequestBody OcorrenciaPriorizacaoDTO dto) {
        return ResponseEntity.ok(priorizacaoService.recalcular(ocorrenciaId, dto));
    }
}
