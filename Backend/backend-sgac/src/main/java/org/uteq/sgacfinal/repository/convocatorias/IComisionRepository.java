package org.uteq.sgacfinal.repository.convocatorias;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.ComisionSeleccion;

@Repository
public interface IComisionRepository extends JpaRepository<ComisionSeleccion, Integer> {
    @Query(value = "SELECT postulacion.fn_generar_comisiones_automaticas()",
            nativeQuery = true)
    String generarComisionesAutomaticas();


    @Query(value = "SELECT postulacion.fn_consultar_comision_detalle(:pIdUsuario, :pRol)",
            nativeQuery = true)
    String consultarComisionDetalle(@Param("pIdUsuario") Integer pIdUsuario,
                                    @Param("pRol")       String  pRol);
}