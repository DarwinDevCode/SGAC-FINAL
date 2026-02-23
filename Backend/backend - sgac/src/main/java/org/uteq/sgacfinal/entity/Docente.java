package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "docente", schema = "academico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Docente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_docente")
    private Integer idDocente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "activo")
    private Boolean activo;

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private List<DocenteAsignatura> docenteAsignaturas = new ArrayList<>();

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private List<Convocatoria> convocatorias = new ArrayList<>();
}