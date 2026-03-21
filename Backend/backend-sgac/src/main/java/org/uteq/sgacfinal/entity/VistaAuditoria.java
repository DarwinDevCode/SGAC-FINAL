package org.uteq.sgacfinal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Table(name = "v_auditoria_completa", schema = "seguridad")
@Immutable
@Getter
public class VistaAuditoria {

    @Id
    @Column(name = "id_log_auditoria")
    private Integer idLogAuditoria;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @Column(name = "nombre_completo_usuario")
    private String nombreCompletoUsuario;

    @Column(name = "cedula")
    private String cedula;

    @Column(name = "accion")
    private String accion;

    @Column(name = "tabla_afectada")
    private String tablaAfectada;

    @Column(name = "ip_origen")
    private String ipOrigen;

    @Column(name = "valor_anterior")
    private String valorAnterior;

    @Column(name = "valor_nuevo")
    private String valorNuevo;
}