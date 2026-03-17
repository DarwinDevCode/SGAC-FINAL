package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ayudantia", nullable = false)
    private Ayudantia ayudantia;

    @Column(name = "mes")
    private Integer mes;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "estado", length = 50)
    private String estado; // GENERADO, REVISADO_DOCENTE, APROBADO_COORDINADOR

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_revision_docente")
    private LocalDateTime fechaRevisionDocente;

    @Column(name = "fecha_aprobacion_coordinador")
    private LocalDateTime fechaAprobacionCoordinador;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
