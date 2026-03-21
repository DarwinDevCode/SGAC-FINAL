package org.uteq.sgacfinal.repository.estudiante;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.dto.response.estudiante.ConvocatoriaEstudianteDTO;
import org.uteq.sgacfinal.entity.Convocatoria;

import java.util.List;

@Repository
public interface IGestionConvocatoria extends JpaRepository<Convocatoria,Integer> {
    @Query(value = "SELECT * FROM convocatoria.fn_listar_convocatorias_estudiante(:idUsuario)", nativeQuery = true)
    List<ConvocatoriaEstudianteDTO> listarConvocatoriasEstudiante(@Param("idUsuario") Integer idUsuario);
}
