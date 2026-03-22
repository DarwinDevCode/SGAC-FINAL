package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "informe_mensual", schema = "ayudantia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InformeMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_informe_mensual")
    private Integer idInformeMensual;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ayudantia", nullable = false)
    private Ayudantia ayudantia;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_periodo_academico", nullable = false)
    private PeriodoAcademico periodoAcademico;

    @NotNull
    @Column(name = "mes", nullable = false)
    private Integer mes;

    @NotNull
    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "contenido_borrador", columnDefinition = "TEXT")
    private String contenidoBorrador;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_estado_informe", nullable = false)
    private TipoEstadoInforme tipoEstadoInforme;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Size(max = 500)
    @Column(name = "firma_path", length = 500)
    private String firmaPath;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
