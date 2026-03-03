package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "notificacion", schema = "notificacion")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion", nullable = false)
    private Integer idNotificacion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Size(max = 150)
    @NotNull
    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @NotNull
    @Column(name = "mensaje", nullable = false, length = Integer.MAX_VALUE)
    private String mensaje;

    @Size(max = 30)
    @NotNull
    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "id_referencia")
    private Integer idReferencia;

    @ColumnDefault("false")
    @Column(name = "leido")
    @Builder.Default
    private Boolean leido = false;

    // OJO: en Postgres la columna real es `fecha_creacion`.
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha_creacion")
    private Instant fechaCreacion;

    // OJO: en Postgres la columna real es `fecha_lectura`.
    @Column(name = "fecha_lectura")
    private Instant fechaLectura;
}
