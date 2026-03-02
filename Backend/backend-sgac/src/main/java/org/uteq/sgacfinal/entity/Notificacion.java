package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion", schema = "notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer idNotificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_destino", nullable = false)
    private Usuario usuarioDestino;

    @Column(name = "mensaje", nullable = false, length = 255)
    private String mensaje;

    @Column(name = "fecha_envio", nullable = false)
    @Builder.Default
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    @Column(name = "leido", nullable = false)
    @Builder.Default
    private Boolean leido = false;

    @Column(name = "tipo", length = 50)
    private String tipo;

    /** P10 — clasificación de notificación: INDIVIDUAL | MASIVA_ROL | MASIVA_TODOS */
    @Column(name = "tipo_notificacion", length = 30)
    @Builder.Default
    private String tipoNotificacion = "INDIVIDUAL";

    /** P10 — convocatoria asociada (puede ser null) */
    @Column(name = "id_convocatoria")
    private Integer idConvocatoria;
}
