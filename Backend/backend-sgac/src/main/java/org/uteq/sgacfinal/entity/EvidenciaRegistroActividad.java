package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "evidencia_registro_actividad", schema = "ayudantia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenciaRegistroActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia_registro_actividad")
    private Integer idEvidenciaRegistroActividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_registro_actividad", nullable = false)
    private RegistroActividad registroActividad;

    @Column(name = "nombre_archivo", length = 150)
    private String nombreArchivo;

    @Column(name = "fecha_subida")
    private LocalDate fechaSubida;

    @Column(name = "activo")
    private Boolean activo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_estado_evidencia", nullable = false)
    private TipoEstadoEvidencia idTipoEstadoEvidencia;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_evidencia", nullable = false)
    private TipoEvidencia idTipoEvidencia;

    @Size(max = 500)
    @NotNull
    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    @Size(max = 100)
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "tamanio_bytes")
    private Integer tamanioBytes;

}