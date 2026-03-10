package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "acta_evaluacion", schema = "postulacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActaEvaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acta")
    private Integer idActa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulacion", nullable = false)
    private Postulacion postulacion;

    @Column(name = "tipo_acta", length = 20, nullable = false)
    private String tipoActa; // MERITOS, OPOSICION

    @Column(name = "url_documento", length = 500)
    private String urlDocumento;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    @Column(name = "confirmado_decano", nullable = false)
    private Boolean confirmadoDecano = false;

    @Column(name = "confirmado_coordinador", nullable = false)
    private Boolean confirmadoCoordinador = false;

    @Column(name = "confirmado_docente", nullable = false)
    private Boolean confirmadoDocente = false;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado = "PENDIENTE"; // PENDIENTE, CONFIRMADO
}
