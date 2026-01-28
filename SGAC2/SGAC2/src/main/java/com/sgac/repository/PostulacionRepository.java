package com.sgac.repository;

import com.sgac.entity.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, Integer> {
    List<Postulacion> findByActivo(Boolean activo);
    List<Postulacion> findByEstadoPostulacion(String estadoPostulacion);
    List<Postulacion> findByConvocatoriaIdConvocatoria(Integer idConvocatoria);
    List<Postulacion> findByEstudianteIdEstudiante(Integer idEstudiante);
}
