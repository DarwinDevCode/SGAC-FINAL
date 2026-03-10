package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "resumen_evaluacion", schema = "postulacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenEvaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resumen")
    private Integer idResumen;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulacion", nullable = false, unique = true)
    private Postulacion postulacion;

    @Column(name = "total_meritos", precision = 5, scale = 2)
    private BigDecimal totalMeritos;

    @Column(name = "promedio_oposicion", precision = 5, scale = 2)
    private BigDecimal promedioOposicion;

    @Column(name = "total_final", precision = 5, scale = 2)
    private BigDecimal totalFinal;

    @Column(name = "estado", length = 20)
    private String estado; // GANADOR, APTO, NO_APTO, DESIERTO, PENDIENTE

    @Column(name = "posicion")
    private Integer posicion;

    @Column(name = "fecha_calculo", nullable = false)
    private LocalDateTime fechaCalculo;
}
