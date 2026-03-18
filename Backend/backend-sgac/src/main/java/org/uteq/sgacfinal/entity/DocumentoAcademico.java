package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "documento_academico", schema = "ayudantia")
public class DocumentoAcademico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento", nullable = false)
    private Integer id;

    @Size(max = 150)
    @NotNull
    @Column(name = "nombre_mostrar", nullable = false, length = 150)
    private String nombreMostrar;

    @Size(max = 500)
    @NotNull
    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    @Size(max = 10)
    @Column(name = "extension", length = 10)
    private String extension;

    @Column(name = "peso_bytes")
    private Integer pesoBytes;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha_subida")
    private Instant fechaSubida;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    private TipoDocumento idTipoDocumento;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_periodo", nullable = false)
    private PeriodoAcademico idPeriodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_convocatoria")
    private Convocatoria idConvocatoria;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_sube", nullable = false)
    private Usuario idUsuarioSube;

    @ColumnDefault("true")
    @Column(name = "activo")
    private Boolean activo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_facultad")
    private Facultad idFacultad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrera")
    private Carrera idCarrera;

}