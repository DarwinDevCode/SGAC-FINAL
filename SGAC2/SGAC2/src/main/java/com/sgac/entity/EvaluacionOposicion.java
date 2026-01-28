package com.sgac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluacion_oposicion")
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

    @OneToOne(fetch = FetchType.LAZY)
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

    @Column(name = "estado", length = 30)
    private String estado;

    @OneToMany(mappedBy = "evaluacionOposicion", cascade = CascadeType.ALL)
    private List<UsuarioComision> usuariosComision = new ArrayList<>();
}
