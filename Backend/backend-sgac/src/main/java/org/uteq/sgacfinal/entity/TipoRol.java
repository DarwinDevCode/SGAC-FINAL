package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_rol", schema = "seguridad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_rol")
    private Integer idTipoRol;

    @Column(name = "nombre_tipo_rol", nullable = false, length = 50, unique = true)
    private String nombreTipoRol;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "tipoRol", cascade = CascadeType.ALL)
    private List<UsuarioTipoRol> usuariosTipoRol = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol_bd")
    private RolBD rolBd;
}