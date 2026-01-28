package com.sgac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "evaluacion_meritos")
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

    @OneToOne(fetch = FetchType.LAZY)
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
}
