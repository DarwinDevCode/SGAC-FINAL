package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "tipo_objeto_seguridad_privilegio", schema = "seguridad")
@Data @NoArgsConstructor @AllArgsConstructor
public class TipoObjetoSeguridadPrivilegio {

    @EmbeddedId
    private TipoObjetoSeguridadPrivilegioId id;

    @ManyToOne
    @MapsId("idTipoObjetoSeguridad")
    @JoinColumn(name = "id_tipo_objeto_seguridad")
    private TipoObjetoSeguridad tipoObjeto;

    @ManyToOne
    @MapsId("idPrivilegio")
    @JoinColumn(name = "id_privilegio")
    private Privilegio privilegio;
}

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
class TipoObjetoSeguridadPrivilegioId implements Serializable {
    private Integer idTipoObjetoSeguridad;
    private Integer idPrivilegio;
}