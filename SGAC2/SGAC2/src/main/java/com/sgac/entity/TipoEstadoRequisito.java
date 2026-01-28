package com.sgac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_estado_requisito")
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

    @OneToMany(mappedBy = "tipoEstadoRequisito", cascade = CascadeType.ALL)
    private List<RequisitoAdjunto> requisitosAdjuntos = new ArrayList<>();
}
