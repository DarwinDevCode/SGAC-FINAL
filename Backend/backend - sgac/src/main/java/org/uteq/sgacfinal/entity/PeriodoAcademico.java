package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "periodo_academico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_periodo_academico")
    private Integer idPeriodoAcademico;

    @Column(name = "nombre_periodo", nullable = false, length = 100)
    private String nombrePeriodo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name="activo")
    private Boolean activo;

    @OneToMany(mappedBy = "periodoAcademico", cascade = CascadeType.ALL)
    private List<Convocatoria> convocatorias = new ArrayList<>();

    @OneToMany(mappedBy = "periodoAcademico", cascade = CascadeType.ALL)
    private List<PeriodoAcademicoRequisitoPostulacion> requisitosConfigurados = new ArrayList<>();
}