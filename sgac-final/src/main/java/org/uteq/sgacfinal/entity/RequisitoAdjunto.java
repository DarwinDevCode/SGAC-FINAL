package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "requisito_adjunto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequisitoAdjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_requisito_adjunto")
    private Integer idRequisitoAdjunto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulacion", nullable = false)
    private Postulacion postulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_requisito_postulacion", nullable = false)
    private TipoRequisitoPostulacion tipoRequisitoPostulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_estado_requisito", nullable = false)
    private TipoEstadoRequisito tipoEstadoRequisito;

    @Column(name = "archivo", columnDefinition = "bytea")
    private byte[] archivo;

    @Column(name = "nombre_archivo", length = 150)
    private String nombreArchivo;

    @Column(name = "fecha_subida")
    private LocalDate fechaSubida;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;
}
