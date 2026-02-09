package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "docente_asignatura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteAsignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_docente_asignatura")
    private Integer idDocenteAsignatura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_asignatura", nullable = false)
    private Asignatura asignatura;

    @Column(name = "activo")
    private Boolean activo;
}