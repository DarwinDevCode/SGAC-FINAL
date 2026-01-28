package com.sgac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "postulacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_postulacion")
    private Integer idPostulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_convocatoria", nullable = false)
    private Convocatoria convocatoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plazo_actividad")
    private PlazoActividad plazoActividad;

    @Column(name = "fecha_postulacion")
    private LocalDate fechaPostulacion;

    @Column(name = "estado_postulacion", length = 30)
    private String estadoPostulacion;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "activo")
    private Boolean activo;

    @OneToMany(mappedBy = "postulacion", cascade = CascadeType.ALL)
    private List<RequisitoAdjunto> requisitosAdjuntos = new ArrayList<>();

    @OneToOne(mappedBy = "postulacion", cascade = CascadeType.ALL)
    private EvaluacionMeritos evaluacionMeritos;

    @OneToOne(mappedBy = "postulacion", cascade = CascadeType.ALL)
    private EvaluacionOposicion evaluacionOposicion;

    @OneToMany(mappedBy = "postulacion", cascade = CascadeType.ALL)
    private List<Ayudantia> ayudantias = new ArrayList<>();
}
