package org.uteq.sgacfinal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "cedula", nullable = false, length = 20, unique = true)
    private String cedula;

    @Column(name = "correo", nullable = false, length = 150, unique = true)
    private String correo;

    @Column(name = "nombre_usuario", nullable = false, length = 50, unique = true)
    private String nombreUsuario;

    @Column(name = "contrasenia_usuario", nullable = false, length = 255)
    private String contraseniaUsuario;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private LocalDate fechaCreacion = LocalDate.now();

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<UsuarioTipoRol> roles = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<Estudiante> estudiantes = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<Docente> docentes = new ArrayList<>();
}