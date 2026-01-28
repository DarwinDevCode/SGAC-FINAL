package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carrera")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrera")
    private Integer idCarrera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_facultad", nullable = false)
    private Facultad facultad;

    @Column(name = "nombre_carrera", nullable = false, length = 150)
    private String nombreCarrera;

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.ALL)
    private List<Asignatura> asignaturas = new ArrayList<>();

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.ALL)
    private List<Estudiante> estudiantes = new ArrayList<>();

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.ALL)
    private List<Coordinador> coordinadores = new ArrayList<>();
}
