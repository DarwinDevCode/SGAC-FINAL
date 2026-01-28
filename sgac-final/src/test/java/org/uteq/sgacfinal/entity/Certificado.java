package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "certificado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_certificado")
    private Integer idCertificado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ayudantia", nullable = false)
    private Ayudantia ayudantia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "codigo_verificacion", length = 50)
    private String codigoVerificacion;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "total_horas_certificadas")
    private Integer totalHorasCertificadas;

    @Lob
    @Column(name = "archivo")
    private byte[] archivo;

    @Column(name = "estado", length = 30)
    private String estado;

    @Column(name = "activo")
    private Boolean activo;
}
