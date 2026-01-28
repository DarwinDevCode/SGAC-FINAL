package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plazo_actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlazoActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plazo_actividad")
    private Integer idPlazoActividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_plazo_actividad", nullable = false)
    private TipoPlazoActividad tipoPlazoActividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_periodo_academico")
    private PeriodoAcademico periodoAcademico;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "activo")
    private Boolean activo;

    @OneToMany(mappedBy = "plazoActividad", cascade = CascadeType.ALL)
    private List<Convocatoria> convocatorias = new ArrayList<>();

    @OneToMany(mappedBy = "plazoActividad", cascade = CascadeType.ALL)
    private List<Postulacion> postulaciones = new ArrayList<>();
}
