package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "privilegio", schema = "seguridad")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Privilegio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_privilegio")
    private Integer idPrivilegio;

    @Column(name = "nombre_privilegio", unique = true, nullable = false, length = 50)
    private String nombrePrivilegio;

    @Column(name = "codigo_interno", length = 1)
    private String codigoInterno;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToMany(mappedBy = "privilegios", fetch = FetchType.LAZY)
    private Set<TipoObjetoSeguridad> tiposObjeto;
}