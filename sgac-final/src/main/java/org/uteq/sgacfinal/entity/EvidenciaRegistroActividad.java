package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "evidencia_registro_actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenciaRegistroActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia_registro_actividad")
    private Integer idEvidenciaRegistroActividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_registro_actividad", nullable = false)
    private RegistroActividad registroActividad;

    @Column(name = "tipo_evidencia", length = 50)
    private String tipoEvidencia;

    //@Lob
    @Column(name = "archivo")
    private byte[] archivo;

    @Column(name = "nombre_archivo", length = 150)
    private String nombreArchivo;

    @Column(name = "fecha_subida")
    private LocalDate fechaSubida;

    @Column(name = "activo")
    private Boolean activo;
}