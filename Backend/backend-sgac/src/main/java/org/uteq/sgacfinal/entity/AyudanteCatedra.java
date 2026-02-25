package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ayudante_catedra", schema = "academico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AyudanteCatedra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ayudante_catedra")
    private Integer idAyudanteCatedra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "horas_ayudante", precision = 5, scale = 2)
    private BigDecimal horasAyudante;

    @OneToMany(mappedBy = "ayudanteCatedra", cascade = CascadeType.ALL)
    private List<SancionAyudanteCatedra> sanciones = new ArrayList<>();
}