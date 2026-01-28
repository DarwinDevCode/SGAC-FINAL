package com.sgac.repository;

import com.sgac.entity.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Integer> {
    List<Certificado> findByAyudantiaIdAyudantia(Integer idAyudantia);
    Optional<Certificado> findByCodigoVerificacion(String codigoVerificacion);
    List<Certificado> findByEstado(String estado);
    List<Certificado> findByActivo(Boolean activo);
}
