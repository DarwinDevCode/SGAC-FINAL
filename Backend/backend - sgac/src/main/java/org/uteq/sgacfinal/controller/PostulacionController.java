package org.uteq.sgacfinal.controller;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.PostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoRequisitoPostulacionResponseDTO;
import org.uteq.sgacfinal.service.IPostulacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.service.ITipoRequisitoPostulacionService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/postulaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PostulacionController {
    private final IPostulacionService postulacionService;
    private final ITipoRequisitoPostulacionService requisitoService;

    @PostMapping(value = "/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registrar(
            @RequestPart("datos") String datosJson,
            @RequestPart("archivos") List<MultipartFile> archivos,
            @RequestParam("tiposRequisito") List<Integer> tiposRequisito
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            PostulacionRequestDTO request = mapper.readValue(datosJson, PostulacionRequestDTO.class);
            if (archivos.size() != tiposRequisito.size())
                return ResponseEntity.badRequest().body("La cantidad de archivos no coincide con los requisitos enviados.");
            return ResponseEntity.ok(postulacionService.registrarPostulacionCompleta(request, archivos, tiposRequisito));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
    }

//    @GetMapping("/listar-activos")
//    public ResponseEntity<List<TipoRequisitoPostulacionResponseDTO>> listar() {
//        return ResponseEntity.ok(requisitoService.listarRequisitosActivos());
//    }
}
