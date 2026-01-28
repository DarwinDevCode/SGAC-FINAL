package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "asignatura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignatura")
    private Integer idAsignatura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrera", nullable = false)
    private Carrera carrera;

    @Column(name = "nombre_asignatura", nullable = false, length = 150)
    private String nombreAsignatura;

    @Column(name = "semestre", nullable = false)
    private Integer semestre;

    @OneToMany(mappedBy = "asignatura", cascade = CascadeType.ALL)
    private List<DocenteAsignatura> docenteAsignaturas = new ArrayList<>();

    @OneToMany(mappedBy = "asignatura", cascade = CascadeType.ALL)
    private List<Convocatoria> convocatorias = new ArrayList<>();
}
