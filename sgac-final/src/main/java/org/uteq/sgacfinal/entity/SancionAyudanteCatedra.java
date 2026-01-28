package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "sancion_ayudante_catedra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SancionAyudanteCatedra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sancion_ayudante_catedra")
    private Integer idSancionAyudanteCatedra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_sancion_ayudante_catedra")
    private TipoSancionAyudanteCatedra tipoSancionAyudanteCatedra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ayudante_catedra")
    private AyudanteCatedra ayudanteCatedra;

    @Column(name = "fecha_sancion")
    private LocalDate fechaSancion;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "motivo", length = 150)
    private String motivo;
}
