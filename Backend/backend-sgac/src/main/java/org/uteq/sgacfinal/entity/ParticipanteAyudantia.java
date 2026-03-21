package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "participante_ayudantia", schema = "ayudantia")
public class ParticipanteAyudantia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participante_ayudantia", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ayudantia", nullable = false)
    private Ayudantia idAyudantia;

    @Size(max = 255)
    @NotNull
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Size(max = 100)
    @Column(name = "curso", length = 100)
    private String curso;

    @Size(max = 20)
    @Column(name = "paralelo", length = 20)
    private String paralelo;

    @ColumnDefault("true")
    @Column(name = "activo")
    private Boolean activo;

    @OneToMany(mappedBy = "idParticipanteAyudantia")
    private Set<DetalleAsistenciaActividad> detalleAsistenciaActividads = new LinkedHashSet<>();

}