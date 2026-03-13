package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.PermisoDTO;
import org.uteq.sgacfinal.dto.Request.FiltroPermisosRequestDTO;
import org.uteq.sgacfinal.dto.Request.GestionPermisosMasivoRequestDTO;
import org.uteq.sgacfinal.dto.Request.GestionPermisosRequestDTO;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.service.IExcelGeneratorService;
import org.uteq.sgacfinal.service.IPermisoService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.List;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PermisoController {

    private final IPermisoService permisoService;
    private final org.uteq.sgacfinal.service.IPdfGeneratorService pdfService;
    private final IExcelGeneratorService excelGeneratorService;


    @GetMapping
    public ResponseEntity<List<PermisoDTO>> permisosActuales() {
        return ResponseEntity.ok(permisoService.obtenerPermisos());
    }

    @GetMapping("/reporte")
    public ResponseEntity<byte[]> descargarReportePermisos() {
        try {
            // We pass a general string for now as representation
            byte[] pdfBytes = pdfService.generarMatrizPermisos("Listado General de Permisos en el Sistema Roles Activos");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=matriz_permisos.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> descargarReportePermisosExcel() {
        try {
            byte[] excelBytes = excelGeneratorService.generarMatrizPermisos();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=matriz_permisos.xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/consultar")
    public ResponseEntity<List<PermisoRolResponseDTO>> consultarPermisosRol(@Valid FiltroPermisosRequestDTO filtro){
        List<PermisoRolResponseDTO> respuesta = permisoService.consultarPermisos(filtro);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/gestionar")
    public ResponseEntity<MensajeResponseDTO> gestionarPermiso(@Valid @RequestBody GestionPermisosRequestDTO request) {

        Boolean exito = permisoService.gestionarPermiso(request);

        if (exito) {
            String accion = request.getOtorgar() ? "otorgado" : "revocado";
            return ResponseEntity.ok(
                    new MensajeResponseDTO("Permiso " + accion + " exitosamente.", true)
            );
        } else
            return ResponseEntity.badRequest().body(
                    new MensajeResponseDTO("Ocurrió un error al intentar gestionar el permiso.", false)
            );
    }

    @GetMapping("/esquemas")
    public ResponseEntity<List<String>> getEsquemas() {
        return ResponseEntity.ok(permisoService.listarEsquemas());
    }

    @GetMapping("/tipos-objeto")
    public ResponseEntity<List<TipoObjetoResponseDTO>> getTiposObjeto() {
        return ResponseEntity.ok(permisoService.listarTiposObjeto());
    }

    @GetMapping("/elementos")
    public ResponseEntity<List<String>> getElementos(
            @RequestParam String esquema,
            @RequestParam String tipoObjeto) {
        return ResponseEntity.ok(permisoService.listarElementos(esquema, tipoObjeto));
    }

    @GetMapping("/privilegios/{idTipoObjeto}")
    public ResponseEntity<List<PrivilegioResponseDTO>> getPrivilegios(
            @PathVariable Integer idTipoObjeto) {
        return ResponseEntity.ok(permisoService.listarPrivilegios(idTipoObjeto));
    }


    @PostMapping("/gestionar-masivo")
    public ResponseEntity<ResultadoMasivoResponseDTO> gestionarPermisosMasivo(
            @Valid @RequestBody GestionPermisosMasivoRequestDTO request) {
        try {
            ResultadoMasivoResponseDTO resultado = permisoService.gestionarPermisosMasivo(request.getPermisos());
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResultadoMasivoResponseDTO.builder()
                            .exito(false)
                            .mensaje(e.getMessage())
                            .totalProcesados(request.getPermisos().size())
                            .exitosos(0)
                            .fallidos(request.getPermisos().size())
                            .build()
            );
        }
    }









//    @PostMapping("/gestionar-masivo")
//    public ResponseEntity<ResultadoMasivoResponseDTO> gestionarPermisosMasivo(
//            @Valid @RequestBody GestionPermisosMasivoRequestDTO request) {
//        ResultadoMasivoResponseDTO resultado = permisoService.gestionarPermisosMasivo(request.getPermisos());
//        return ResponseEntity.ok(resultado);
//    }





//    @GetMapping("/elementos")
//    public ResponseEntity<List<ElementoBdResponseDTO>> listarElementos(
//            @RequestParam String esquema,
//            @RequestParam String categoria){
//        List<ElementoBdResponseDTO> elementos = permisoService.listarElementos(esquema, categoria);
//        return ResponseEntity.ok(elementos);
//    }

//    @GetMapping("/esquemas")
//    public ResponseEntity<List<EsquemaResponseDTO>> listarEsquemas(){
//        return ResponseEntity.ok(permisoService.listarEsquemas());
//    }
}
