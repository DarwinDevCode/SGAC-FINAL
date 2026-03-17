package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "evaluacion_meritos", schema = "postulacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionMeritos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion_meritos")
    private Integer idEvaluacionMeritos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulacion", nullable = false)
    private Postulacion postulacion;

    @Column(name = "nota_asignatura", precision = 5, scale = 2)
    private BigDecimal notaAsignatura;

    @Column(name = "nota_semestres", precision = 5, scale = 2)
    private BigDecimal notaSemestres;

    @Column(name = "nota_eventos", precision = 5, scale = 2)
    private BigDecimal notaEventos;

    @Column(name = "nota_experiencia", precision = 5, scale = 2)
    private BigDecimal notaExperiencia;

    @Column(name = "fecha_evaluacion")
    private LocalDate fechaEvaluacion;

    @ColumnDefault("(((nota_asignatura + nota_semestres) + nota_eventos) + nota_experiencia)")
    @Column(name = "nota_total_meritos", precision = 5, scale = 2)
    private BigDecimal notaTotalMeritos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_estado_evaluacion")
    private TipoEstadoEvaluacion idTipoEstadoEvaluacion;

}