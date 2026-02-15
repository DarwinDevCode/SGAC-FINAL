package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Certificado;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Integer> {

    @Query(value = "SELECT public.sp_crear_certificado(:idAyudantia, :idUsuario, :codigo, :fecha, :horas, :archivo, :estado)", nativeQuery = true)
    Integer registrarCertificado(@Param("idAyudantia") Integer idAyudantia,
                                 @Param("idUsuario") Integer idUsuario,
                                 @Param("codigo") String codigoVerificacion,
                                 @Param("fecha") LocalDate fechaEmision,
                                 @Param("horas") Integer totalHoras,
                                 @Param("archivo") byte[] archivo,
                                 @Param("estado") String estado);

    @Query(value = "SELECT public.sp_actualizar_certificado(:id, :estado, :archivo)", nativeQuery = true)
    Integer actualizarCertificado(@Param("id") Integer idCertificado,
                                  @Param("estado") String estado,
                                  @Param("archivo") byte[] archivo);

    @Query(value = "SELECT public.sp_desactivar_certificado(:id)", nativeQuery = true)
    Integer desactivarCertificado(@Param("id") Integer idCertificado);

    @Query(value = "SELECT * FROM public.sp_listar_certificados()", nativeQuery = true)
    List<Object[]> listarCertificadosActivosSP();
}