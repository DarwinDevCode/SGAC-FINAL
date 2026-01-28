package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_plazo_actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoPlazoActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_plazo_actividad")
    private Integer idTipoPlazoActividad;

    @Column(name = "nombre_tipo", length = 50)
    private String nombreTipo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @OneToMany(mappedBy = "tipoPlazoActividad", cascade = CascadeType.ALL)
    private List<PlazoActividad> plazosActividad = new ArrayList<>();
}
