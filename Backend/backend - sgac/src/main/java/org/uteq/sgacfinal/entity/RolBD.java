package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rol_bd", schema = "seguridad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolBD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol_bd")
    private Integer idRolBd;

    @Column(name = "nombre_rol_bd", nullable = false, unique = true, length = 100)
    private String nombreRolBd;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

}