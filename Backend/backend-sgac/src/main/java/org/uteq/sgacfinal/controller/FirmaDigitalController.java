package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Response.ActaEvaluacionResponseDTO;
import org.uteq.sgacfinal.service.IActaEvaluacionService;

@RestController
@RequestMapping("/api/firmadigital/actas")
@RequiredArgsConstructor
public class FirmaDigitalController {

    private final IActaEvaluacionService actaEvaluacionService;

    @PostMapping(path = "/{idActa}/firmar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ActaEvaluacionResponseDTO> firmarActa(
            @PathVariable Integer idActa,
            @RequestParam("archivoFirma") MultipartFile archivoFirma,
            @RequestParam("password") String password,
            @RequestParam("rolFirmante") String rolFirmante) {

        ActaEvaluacionResponseDTO response = actaEvaluacionService.firmarActa(idActa, archivoFirma, password, rolFirmante);
        return ResponseEntity.ok(response);
    }
}
