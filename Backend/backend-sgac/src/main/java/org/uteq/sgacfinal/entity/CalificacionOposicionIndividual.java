package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "calificacion_oposicion_individual", schema = "postulacion",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_postulacion", "id_evaluador"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalificacionOposicionIndividual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_calificacion")
    private Integer idCalificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulacion", nullable = false)
    private Postulacion postulacion;

    @Column(name = "id_evaluador", nullable = false)
    private Integer idEvaluador;

    @Column(name = "rol_evaluador", length = 20, nullable = false)
    private String rolEvaluador; // DECANO, COORDINADOR, DOCENTE

    @Column(name = "criterio_material", precision = 5, scale = 2, nullable = false)
    private BigDecimal criterioMaterial;   // 0–10

    @Column(name = "criterio_calidad", precision = 5, scale = 2, nullable = false)
    private BigDecimal criterioCalidad;    // 0–4

    @Column(name = "criterio_pertinencia", precision = 5, scale = 2, nullable = false)
    private BigDecimal criterioPertinencia; // 0–6

    /**
     * Columna generada en BD: criterio_material + criterio_calidad + criterio_pertinencia
     * Se marca insertable=false, updatable=false para que Hibernate no intente escribirla.
     */
    @Column(name = "subtotal", precision = 5, scale = 2, insertable = false, updatable = false)
    private BigDecimal subtotal;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}
