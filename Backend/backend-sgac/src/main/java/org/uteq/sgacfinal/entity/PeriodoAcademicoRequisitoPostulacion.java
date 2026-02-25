package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "periodo_academico_requisito_postulacion", schema = "convocatoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoAcademicoRequisitoPostulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_periodo_academico_requisito_postulacion")
    private Integer idPeriodoAcademicoRequisitoPostulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_periodo_academico", nullable = false)
    private PeriodoAcademico periodoAcademico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_requisito_postulacion", nullable = false)
    private TipoRequisitoPostulacion tipoRequisitoPostulacion;

    @Column(name = "obligatorio")
    private Boolean obligatorio;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "activo")
    private Boolean activo;
}