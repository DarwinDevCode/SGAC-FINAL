package com.sgac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_estado_evidencia_ayudantia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoEstadoEvidenciaAyudantia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_estado_evidencia_ayudantia")
    private Integer idTipoEstadoEvidenciaAyudantia;

    @Column(name = "nombre_estado", length = 50)
    private String nombreEstado;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @OneToMany(mappedBy = "tipoEstadoEvidenciaAyudantia", cascade = CascadeType.ALL)
    private List<Ayudantia> ayudantias = new ArrayList<>();
}
