package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "banco_temas", schema = "postulacion")
public class BancoTema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tema", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_convocatoria", nullable = false)
    private Convocatoria idConvocatoria;

    @Size(max = 255)
    @NotNull
    @Column(name = "descripcion_tema", nullable = false)
    private String descripcionTema;

    @ColumnDefault("true")
    @Column(name = "activo")
    private Boolean activo;

}