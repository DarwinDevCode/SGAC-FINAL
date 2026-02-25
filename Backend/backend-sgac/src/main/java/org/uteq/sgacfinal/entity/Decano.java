package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "decano", schema = "academico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Decano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_decano")
    private Integer idDecano;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_facultad", nullable = false)
    private Facultad facultad;

    @Column(name = "fecha_inicio_gestion", nullable = false)
    private LocalDate fechaInicioGestion;

    @Column(name = "fecha_fin_gestion")
    private LocalDate fechaFinGestion;

    @Column(name = "activo")
    private Boolean activo;
}