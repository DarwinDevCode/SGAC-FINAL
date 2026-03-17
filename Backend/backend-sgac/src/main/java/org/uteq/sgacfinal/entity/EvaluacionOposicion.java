package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluacion_oposicion", schema = "postulacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionOposicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion_oposicion")
    private Integer idEvaluacionOposicion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulacion", nullable = false)
    private Postulacion postulacion;

    @Column(name = "tema_exposicion", length = 150)
    private String temaExposicion;

    @Column(name = "fecha_evaluacion")
    private LocalDate fechaEvaluacion;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "lugar", length = 100)
    private String lugar;

    @Column(name = "orden_exposicion")
    private Integer ordenExposicion;

    @Column(name = "hora_inicio_real")
    private LocalTime horaInicioReal;

    @Column(name = "hora_fin_real")
    private LocalTime horaFinReal;

    @Column(name = "puntaje_total_oposicion", precision = 5, scale = 2)
    private BigDecimal puntajeTotalOposicion;

    @Column(name = "puntaje_material", precision = 5, scale = 2)
    private BigDecimal puntajeMaterial;

    @Column(name = "puntaje_exposicion", precision = 5, scale = 2)
    private BigDecimal puntajeExposicion;

    @Column(name = "puntaje_respuestas", precision = 5, scale = 2)
    private BigDecimal puntajeRespuestas;

    @Column(name = "tema_sorteado", length = 200)
    private String temaSorteado;

    @Column(name = "timestamp_sorteo")
    private LocalDateTime timestampSorteo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_estado_evaluacion")
    private TipoEstadoEvaluacion idTipoEstadoEvaluacion;

    @OneToMany(mappedBy = "evaluacionOposicion", cascade = CascadeType.ALL)
    private List<UsuarioComision> usuariosComision = new ArrayList<>();
}