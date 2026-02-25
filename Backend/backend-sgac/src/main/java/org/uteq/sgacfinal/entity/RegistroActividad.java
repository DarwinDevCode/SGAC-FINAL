package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "registro_actividad", schema = "ayudantia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro_actividad")
    private Integer idRegistroActividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ayudantia", nullable = false)
    private Ayudantia ayudantia;

    @Column(name = "descripcion_actividad", columnDefinition = "TEXT")
    private String descripcionActividad;

    @Column(name = "tema_tratado", columnDefinition = "TEXT")
    private String temaTratado;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "numero_asistentes")
    private Integer numeroAsistentes;

    @Column(name = "horas_dedicadas", precision = 5, scale = 2)
    private BigDecimal horasDedicadas;

    @Column(name = "estado_revision", length = 30)
    private String estadoRevision;

    @OneToMany(mappedBy = "registroActividad", cascade = CascadeType.ALL)
    private List<EvidenciaRegistroActividad> evidencias = new ArrayList<>();
}