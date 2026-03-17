package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "tipo_estado_evaluacion", schema = "postulacion")
public class TipoEstadoEvaluacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_estado_evaluacion", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Size(max = 30)
    @NotNull
    @Column(name = "codigo", nullable = false, length = 30)
    private String codigo;

    @Column(name = "descripcion", length = Integer.MAX_VALUE)
    private String descripcion;

    @ColumnDefault("true")
    @Column(name = "activo")
    private Boolean activo;

}