package org.uteq.sgacfinal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_tipo_rol", schema = "seguridad")
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
    @JsonIgnoreProperties({"roles", "docentes", "estudiantes"})
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idTipoRol")
    @JoinColumn(name = "id_tipo_rol")
    @JsonIgnoreProperties("usuarioTipoRoles")
    private TipoRol tipoRol;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}