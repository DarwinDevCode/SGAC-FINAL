package com.sgac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioTipoRolId implements Serializable {

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_tipo_rol")
    private Integer idTipoRol;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioTipoRolId that = (UsuarioTipoRolId) o;
        return Objects.equals(idUsuario, that.idUsuario) && Objects.equals(idTipoRol, that.idTipoRol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, idTipoRol);
    }
}
