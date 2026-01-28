package com.sgac.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario_tipo_rol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioTipoRol {

    @EmbeddedId
    private UsuarioTipoRolId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario")
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idTipoRol")
    @JoinColumn(name = "id_tipo_rol")
    private TipoRol tipoRol;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}
