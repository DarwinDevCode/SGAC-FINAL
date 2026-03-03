package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "asignatura", schema = "academico")
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
    @NotBlank(message = "El nombre de la asignatura es requerido")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombreAsignatura;

    @Column(name = "semestre", nullable = false)
    @Min(value = 1, message = "El semestre debe ser mayor a 0")
    @Max(value = 10, message = "El semestre no puede ser mayor a 10")
    private Integer semestre;

    @Column(name="activo")
    private Boolean activo;

    @OneToMany(mappedBy = "asignatura", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<DocenteAsignatura> docenteAsignaturas = new ArrayList<>();

    @OneToMany(mappedBy = "asignatura", cascade = CascadeType.PERSIST)
    private List<Convocatoria> convocatorias = new ArrayList<>();
}