package com.sgac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log_auditoria")
    private Integer idLogAuditoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "accion", length = 100)
    private String accion;

    @Column(name = "tabla_afectada", length = 100)
    private String tablaAfectada;

    @Column(name = "registro_afectado")
    private Integer registroAfectado;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "ip_origen", length = 50)
    private String ipOrigen;

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "TEXT")
    private String valorNuevo;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }
}
