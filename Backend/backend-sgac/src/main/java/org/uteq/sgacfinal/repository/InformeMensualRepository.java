package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.InformeMensual;
import java.util.List;
import java.util.Optional;

@Repository
public interface InformeMensualRepository extends JpaRepository<InformeMensual, Integer> {
    List<InformeMensual> findByAyudantiaIdAyudantia(Integer idAyudantia);
    List<InformeMensual> findByAyudantiaIdAyudantiaOrderByAnioDescMesDesc(Integer idAyudantia);
    Optional<InformeMensual> findByAyudantiaIdAyudantiaAndMesAndAnio(Integer idAyudantia, Integer mes, Integer anio);
    List<InformeMensual> findByEstado(String estado);
    List<InformeMensual> findByEstadoAndAyudantiaPostulacionConvocatoriaDocenteIdDocente(String estado, Integer idDocente);
}
