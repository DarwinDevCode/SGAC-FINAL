package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluacion_desempeno", schema = "ayudantia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionDesempeno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion_desempeno")
    private Integer idEvaluacionDesempeno;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_registro_actividad", nullable = false)
    private RegistroActividad registroActividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    @Column(name = "puntaje")
    private Integer puntaje;

    @Column(name = "retroalimentacion", columnDefinition = "TEXT")
    private String retroalimentacion;

    @Column(name = "fecha_evaluacion")
    private LocalDateTime fechaEvaluacion;
}
