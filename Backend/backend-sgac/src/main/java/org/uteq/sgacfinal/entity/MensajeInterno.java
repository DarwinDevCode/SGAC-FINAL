package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensaje_interno", schema = "comunicacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeInterno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje_interno")
    private Integer idMensajeInterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ayudantia", nullable = false)
    private Ayudantia ayudantia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_emisor", nullable = false)
    private Usuario emisor;

    @Column(name = "mensaje", columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "ruta_archivo_adjunto", length = 500)
    private String rutaArchivoAdjunto;

    @Column(name = "leido")
    private Boolean leido;
}
