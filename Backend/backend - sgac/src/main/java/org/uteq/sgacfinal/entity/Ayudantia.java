package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ayudantia", schema = "ayudantia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ayudantia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ayudantia")
    private Integer idAyudantia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_estado_evidencia_ayudantia", nullable = false)
    private TipoEstadoEvidenciaAyudantia tipoEstadoEvidenciaAyudantia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulacion", nullable = false)
    private Postulacion postulacion;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "horas_cumplidas")
    private Integer horasCumplidas;

    @OneToMany(mappedBy = "ayudantia", cascade = CascadeType.ALL)
    private List<Certificado> certificados = new ArrayList<>();

    @OneToMany(mappedBy = "ayudantia", cascade = CascadeType.ALL)
    private List<RegistroActividad> registrosActividad = new ArrayList<>();
}