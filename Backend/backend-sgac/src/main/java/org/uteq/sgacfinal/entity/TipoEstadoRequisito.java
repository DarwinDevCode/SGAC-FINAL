package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_estado_requisito", schema = "convocatoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoEstadoRequisito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_estado_requisito")
    private Integer idTipoEstadoRequisito;

    @Column(name = "nombre_estado", length = 50)
    private String nombreEstado;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name="activo")
    private Boolean activo;

    @OneToMany(mappedBy = "tipoEstadoRequisito", cascade = CascadeType.ALL)
    private List<RequisitoAdjunto> requisitosAdjuntos = new ArrayList<>();

    @Size(max = 25)
    @NotNull
    @Column(name = "codigo", nullable = false, length = 25)
    private String codigo;

}