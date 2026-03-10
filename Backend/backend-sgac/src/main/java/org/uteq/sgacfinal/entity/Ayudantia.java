package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ayudantia", schema = "ayudantia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ayudantia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ayudantia")
    private Integer idAyudantia;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_estado_ayudantia", nullable = false)
    private TipoEstadoAyudantia idTipoEstadoAyudantia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulacion", nullable = false)
    private Postulacion postulacion;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "horas_cumplidas")
    private Integer horasCumplidas;

    @NotNull
    @ColumnDefault("20")
    @Column(name = "horas_semanales_max", nullable = false, precision = 5, scale = 2)
    private BigDecimal horasSemanalesMax;

    @Column(name = "horas_maximas", precision = 5, scale = 2)
    private BigDecimal horasMaximas;

    @OneToMany(mappedBy = "ayudantia", cascade = CascadeType.ALL)
    private List<Certificado> certificados = new ArrayList<>();

    @OneToMany(mappedBy = "ayudantia", cascade = CascadeType.ALL)
    private List<RegistroActividad> registrosActividad = new ArrayList<>();
}