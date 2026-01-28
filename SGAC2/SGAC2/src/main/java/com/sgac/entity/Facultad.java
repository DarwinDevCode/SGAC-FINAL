package com.sgac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facultad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facultad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_facultad")
    private Integer idFacultad;

    @Column(name = "nombre_facultad", nullable = false, length = 150)
    private String nombreFacultad;

    @OneToMany(mappedBy = "facultad", cascade = CascadeType.ALL)
    private List<Carrera> carreras = new ArrayList<>();

    @OneToMany(mappedBy = "facultad", cascade = CascadeType.ALL)
    private List<Decano> decanos = new ArrayList<>();
}
