package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Estudiante;

import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {

    @Query(value = "SELECT public.sp_crear_estudiante(:idUsuario, :idCarrera, :matricula, :semestre, :estado)", nativeQuery = true)
    Integer registrarEstudiante(@Param("idUsuario") Integer idUsuario,
                                @Param("idCarrera") Integer idCarrera,
                                @Param("matricula") String matricula,
                                @Param("semestre") Integer semestre,
                                @Param("estado") String estadoAcademico);

    @Query(value = "SELECT public.sp_actualizar_estudiante(:id, :idCarrera, :semestre, :estado)", nativeQuery = true)
    Integer actualizarEstudiante(@Param("id") Integer idEstudiante,
                                 @Param("idCarrera") Integer idCarrera,
                                 @Param("semestre") Integer semestre,
                                 @Param("estado") String estadoAcademico);

    @Query(value = "SELECT * FROM public.sp_obtener_estudiante_por_matricula(:matricula)", nativeQuery = true)
    Optional<Estudiante> buscarPorMatriculaSP(@Param("matricula") String matricula);

    Optional<Estudiante> findByUsuario_IdUsuario(Integer idUsuario);
}