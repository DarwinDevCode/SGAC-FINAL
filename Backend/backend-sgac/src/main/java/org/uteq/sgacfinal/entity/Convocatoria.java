package org.uteq.sgacfinal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "convocatoria", schema = "convocatoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Convocatoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_convocatoria")
    private Integer idConvocatoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_periodo_academico", nullable = false)
    private PeriodoAcademico periodoAcademico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_asignatura", nullable = false)
    private Asignatura asignatura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    @JsonIgnoreProperties({"convocatorias", "docenteAsignaturas", "usuario"})
    private Docente docente;

    @Column(name = "cupos_disponibles", nullable = false)
    private Integer cuposDisponibles;

    @Column(name = "estado", length = 30)
    private String estado;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "fecha_inicio_postulacion")
    private LocalDate fechaInicioPostulacion;

    @Column(name = "fecha_fin_postulacion")
    private LocalDate fechaFinPostulacion;

    @Column(name = "fecha_publicacion_resultados")
    private LocalDate fechaPublicacionResultados;

    @OneToMany(mappedBy = "convocatoria", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("convocatoria")
    private List<Postulacion> postulaciones = new ArrayList<>();

    @OneToMany(mappedBy = "convocatoria", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("convocatoria")
    private List<ComisionSeleccion> comisionesSeleccion = new ArrayList<>();
}