package com.sgac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "usuario_comision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioComision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario_comision")
    private Integer idUsuarioComision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comision_seleccion", nullable = false)
    private ComisionSeleccion comisionSeleccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion_oposicion", nullable = false)
    private EvaluacionOposicion evaluacionOposicion;

    @Column(name = "rol_integrante", length = 50)
    private String rolIntegrante;

    @Column(name = "puntaje_material", precision = 5, scale = 2)
    private BigDecimal puntajeMaterial;

    @Column(name = "puntaje_respuestas", precision = 5, scale = 2)
    private BigDecimal puntajeRespuestas;

    @Column(name = "puntaje_exposicion", precision = 5, scale = 2)
    private BigDecimal puntajeExposicion;

    @Column(name = "fecha_evaluacion")
    private LocalDate fechaEvaluacion;
}
