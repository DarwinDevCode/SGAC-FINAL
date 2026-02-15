# Guía de reaplicación: Gestión de usuarios + JWT + `SET LOCAL ROLE`

Este documento resume **qué se trabajó** y da instrucciones para que otra versión del agente (o cualquier desarrollador) pueda reaplicar los cambios en caso de que no estén en GitHub.

## 1) Objetivo funcional

- Mantener autenticación con JWT.
- Permitir que el backend, aunque use un único usuario de conexión JDBC, ejecute operaciones con el **rol de BD del usuario autenticado**.
- Completar la UI de administración para registrar usuarios por tipo (estudiante, docente, coordinador, decano, administrador, ayudante).

## 2) Backend: cambios que deben existir

### 2.1. Resolver `LazyInitializationException` en seguridad

1. En `IUsuariosRepository`, agregar consulta con fetch para login:
   - `findByNombreUsuarioWithRolesAndTipoRol(String nombreUsuario)`
   - Con `LEFT JOIN FETCH u.roles ur LEFT JOIN FETCH ur.tipoRol`

2. En `SecurityConfig.userDetailsService()`, usar ese método con fetch y no la búsqueda simple.

3. Para listado de usuarios (`/api/auth`), agregar y usar:
   - `findAllWithRolesAndTipoRol()` con `DISTINCT` + `JOIN FETCH`.

> Nota: esto evita que `UsuarioPrincipal.getAuthorities()` rompa al acceder a `tipoRol` fuera de sesión.

### 2.2. Propagar identidad del JWT al contexto de request

1. Crear/usar `UserContext` con `ThreadLocal<String>` para username.
2. En `JwtAuthenticationFilter`, después de validar token:
   - `UserContext.setUsername(username);`
3. En `finally` del filtro, limpiar:
   - `UserContext.clear();`

### 2.3. Aplicar impersonación de BD con `SET LOCAL ROLE`

En `UsuariosImpl` (u otra capa de servicio transaccional), implementar método interno tipo:

```java
private void applyCurrentDbRole() {
    String username = UserContext.getUsername();
    if (username == null || username.isBlank()) {
        throw new IllegalStateException("No hay usuario autenticado para aplicar rol de BD");
    }

    entityManager.createNativeQuery("SELECT set_config('role', :rol, true)")
            .setParameter("rol", username.toLowerCase())
            .getSingleResult();
}
```

Regla de uso:
- Llamar `applyCurrentDbRole()` **al inicio de cada método `@Transactional`** que ejecute SP/consultas con permisos dependientes del usuario.
- Priorizarlo en:
  - `registrarEstudiante`
  - `registrarDocente`
  - `registrarCoordinador`
  - `registrarDecano`
  - `registrarAdministrador`
  - `registrarAyudante`
  - `listarTodos` (si quieres consistencia de permisos por sesión lógica)

### 2.4. Permisos SQL necesarios

Tu error `permission denied to create role` viene de PostgreSQL, no del frontend.

Para que el usuario físico de conexión (`app_user_default`) pueda ejecutar SPs que crean usuarios/roles:

- Opción A (recomendada): procedimientos con `SECURITY DEFINER` y owner con privilegios.
- Opción B: otorgar privilegios suficientes al usuario de conexión (p.ej. `CREATEROLE`) y membresías necesarias.

Además, si usas impersonación por username:
- Mantener `GRANT <usuario_bd_creado> TO app_user_default` al registrar.

## 3) Frontend (Angular): cambios que deben existir

En `frontend-sgac/src/app/features/admin/gestion-usuarios`:

1. UI de "Nuevo usuario" con modal.
2. Formulario por rol con campos condicionales:
   - Estudiante: carrera, matrícula, semestre.
   - Coordinador: carrera.
   - Decano: facultad.
   - Ayudante: horas.
3. Validaciones antes de enviar (carrera/facultad según rol).
4. Al guardar:
   - consumir endpoint de creación correcto por rol.
   - recargar lista.
5. Manejo de errores mejorado:
   - duplicate key -> mensaje amigable.
   - `permission denied to create role` -> mensaje de permisos DB/`SECURITY DEFINER`.

## 4) Checklist rápido de verificación

### Backend
- Login admin funciona.
- `GET /api/auth` responde 200 (sin `LazyInitializationException`).
- `POST /api/auth/registro-estudiante`:
  - si hay permisos: 200/201.
  - si no hay permisos: 400 con mensaje explícito de BD.

### Frontend
- En Gestión de Usuarios se abre el modal de creación.
- Campos condicionales cambian según rol.
- Se muestran mensajes de error claros para duplicados/permisos DB.

## 5) Orden recomendado para reaplicar en otra rama

1. Fix de fetch eager controlado (`JOIN FETCH`) en repositorio + security.
2. `UserContext` + `JwtAuthenticationFilter` (set/clear username).
3. `applyCurrentDbRole()` en servicios transaccionales.
4. Ajustes SQL de privilegios (`SECURITY DEFINER` o grants).
5. UI Angular de registro + validaciones + errores.

Con este orden, primero estabilizas autenticación/autoridades y luego habilitas el flujo completo de creación de usuarios.
