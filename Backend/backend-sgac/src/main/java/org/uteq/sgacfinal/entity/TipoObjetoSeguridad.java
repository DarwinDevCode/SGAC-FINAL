package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "tipo_objeto_seguridad", schema = "seguridad")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoObjetoSeguridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_objeto_seguridad")
    private Integer idTipoObjetoSeguridad;

    @Column(name = "nombre_tipo_objeto", unique = true, nullable = false, length = 50)
    private String nombreTipoObjeto;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tipo_objeto_seguridad_privilegio",
            schema = "seguridad",
            joinColumns = @JoinColumn(name = "id_tipo_objeto_seguridad"),
            inverseJoinColumns = @JoinColumn(name = "id_privilegio")
    )
    private Set<Privilegio> privilegios;
}