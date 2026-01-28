package com.sgac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tipo_rol")
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

    @Column(name = "nombre_tipo_rol", unique = true, nullable = false, length = 50)
    private String nombreTipoRol;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "tipoRol", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioTipoRol> usuarioTipoRoles = new HashSet<>();
}
