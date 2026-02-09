package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;

@Repository
public interface ISeguridadRepository {

    @Query(value = "SET ROLE ?1", nativeQuery = true)
    void setRole(String rol);

    @Query(value = """
        SELECT 
            current_user AS rol,
            table_name AS objeto,
            privilege_type AS permiso
        FROM information_schema.role_table_grants
        WHERE grantee = current_user
        """, nativeQuery = true)
    List<Object[]> permisosActuales();
}
