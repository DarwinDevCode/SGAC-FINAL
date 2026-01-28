package com.sgac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comision_seleccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComisionSeleccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comision_seleccion")
    private Integer idComisionSeleccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_convocatoria", nullable = false)
    private Convocatoria convocatoria;

    @Column(name = "nombre_comision", length = 100)
    private String nombreComision;

    @Column(name = "fecha_conformacion")
    private LocalDate fechaConformacion;

    @Column(name = "activo")
    private Boolean activo;

    @OneToMany(mappedBy = "comisionSeleccion", cascade = CascadeType.ALL)
    private List<UsuarioComision> usuariosComision = new ArrayList<>();
}
