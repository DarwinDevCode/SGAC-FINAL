package com.sgac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_requisito_postulacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoRequisitoPostulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_requisito_postulacion")
    private Integer idTipoRequisitoPostulacion;

    @Column(name = "nombre_requisito", nullable = false, length = 100)
    private String nombreRequisito;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "activo")
    private Boolean activo;

    @OneToMany(mappedBy = "tipoRequisitoPostulacion", cascade = CascadeType.ALL)
    private List<RequisitoAdjunto> requisitosAdjuntos = new ArrayList<>();

    @OneToMany(mappedBy = "tipoRequisitoPostulacion", cascade = CascadeType.ALL)
    private List<PeriodoAcademicoRequisitoPostulacion> periodosAcademicosRequisitos = new ArrayList<>();
}
