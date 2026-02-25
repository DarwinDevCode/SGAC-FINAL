package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estudiante", schema = "academico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante")
    private Integer idEstudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrera", nullable = false)
    private Carrera carrera;

    @Column(name = "matricula", nullable = false, length = 30, unique = true)
    private String matricula;

    @Column(name = "semestre", nullable = false)
    private Integer semestre;

    @Column(name = "estado_academico", length = 30)
    private String estadoAcademico;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    private List<Postulacion> postulaciones = new ArrayList<>();
}