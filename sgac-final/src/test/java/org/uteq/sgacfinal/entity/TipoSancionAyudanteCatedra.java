package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_sancion_ayudante_catedra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoSancionAyudanteCatedra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_sancion_ayudante_catedra")
    private Integer idTipoSancionAyudanteCatedra;

    @Column(name = "nombre_tipo_sancion", length = 100)
    private String nombreTipoSancion;

    @Column(name = "activo")
    private Boolean activo;

    @OneToMany(mappedBy = "tipoSancionAyudanteCatedra", cascade = CascadeType.ALL)
    private List<SancionAyudanteCatedra> sanciones = new ArrayList<>();
}
