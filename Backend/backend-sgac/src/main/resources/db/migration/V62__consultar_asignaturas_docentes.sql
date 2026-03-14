CREATE OR REPLACE FUNCTION academico.fn_listar_asignaturas_docente(
    p_id_docente INTEGER
)
    RETURNS JSONB
    LANGUAGE sql
    STABLE
    SECURITY DEFINER
AS $$
SELECT COALESCE(
               jsonb_agg(
                       jsonb_build_object(
                               'idAsignatura',     a.id_asignatura,
                               'nombreAsignatura', a.nombre_asignatura,
                               'semestre',         a.semestre,
                               'idCarrera',        c.id_carrera,
                               'nombreCarrera',    c.nombre_carrera,
                               'idFacultad',       f.id_facultad,
                               'nombreFacultad',   f.nombre_facultad,
                               'etiqueta',         a.nombre_asignatura
                                                       || ' · ' || c.nombre_carrera
                                                       || ' · ' || f.nombre_facultad
                       )
                       ORDER BY f.nombre_facultad, c.nombre_carrera, a.semestre, a.nombre_asignatura
               ),
               '[]'::JSONB
       )
FROM academico.docente_asignatura da
         JOIN academico.asignatura a ON a.id_asignatura = da.id_asignatura
         JOIN academico.carrera    c ON c.id_carrera    = a.id_carrera
         JOIN academico.facultad   f ON f.id_facultad   = c.id_facultad
WHERE da.id_docente = p_id_docente
  AND da.activo     = TRUE;
$$;

COMMENT ON FUNCTION academico.fn_listar_asignaturas_docente(INTEGER) IS
    'Retorna las asignaturas activas asignadas a un docente con jerarquía completa.';