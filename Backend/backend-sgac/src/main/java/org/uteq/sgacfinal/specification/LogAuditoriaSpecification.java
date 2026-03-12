package org.uteq.sgacfinal.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.uteq.sgacfinal.entity.LogAuditoria;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogAuditoriaSpecification {

    public static Specification<LogAuditoria> conFiltros(String queryParams, String tablaAfectada, String accion, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (queryParams != null && !queryParams.isEmpty()) {
                String searchPattern = "%" + queryParams.toLowerCase() + "%";
                Predicate porUsuario = criteriaBuilder.like(criteriaBuilder.lower(root.get("usuario").get("nombres")), searchPattern);
                Predicate porApellido = criteriaBuilder.like(criteriaBuilder.lower(root.get("usuario").get("apellidos")), searchPattern);
                predicates.add(criteriaBuilder.or(porUsuario, porApellido));
            }

            if (tablaAfectada != null && !tablaAfectada.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("tablaAfectada"), tablaAfectada));
            }

            if (accion != null && !accion.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("accion"), accion));
            }

            if (fechaInicio != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fechaHora"), fechaInicio));
            }

            if (fechaFin != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fechaHora"), fechaFin));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
