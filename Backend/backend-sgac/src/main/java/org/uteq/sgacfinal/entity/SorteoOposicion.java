package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sorteo_oposicion", schema = "postulacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SorteoOposicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sorteo")
    private Integer idSorteo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulacion", nullable = false)
    private Postulacion postulacion;

    @Column(name = "tema_sorteado", length = 500, nullable = false)
    private String temaSorteado;

    @Column(name = "semilla_sorteo")
    private Long semillaSorteo;

    @Column(name = "fecha_sorteo", nullable = false)
    private LocalDateTime fechaSorteo;

    @Column(name = "notificado", nullable = false)
    private Boolean notificado = false;
}
