# Decisiones de Diseño y Arquitectura (SGAC)

## 1. Patrón Arquitectónico y Capas
- **Frontend (Angular 18 Standalone):** SPA con Tailwind CSS. Comunicación mediante servicios HTTP inyectables y Guards de ruta.
- **Backend (Spring Boot 3 / Java 21):** Arquitectura N-Tier (Controller -> Service -> Repository -> Entity).
- **Base de Datos (PostgreSQL 16):** Lógica intensiva mediante Procedimientos Almacenados (SPs) y triggers, estructurada en 7 esquemas independientes (academico, ayudantia, convocatoria, notificacion, planificacion, postulacion, seguridad).

## 2. Decisiones Recientes (Fase 1)
### 2.1. Seguridad y JWT
- Se eliminó el Fallback de secreto JWT débil que permitía vulnerar el sistema.
- El secreto ahora es inyectado exclusivamente mediante variables de entorno (IA_API_KEY, JWT_SECRET).

### 2.2. Control de Acceso (Guards y @PreAuthorize)
- Se implementó un esquema de doble validación:
  - **Frontend:** uth.guard.ts (Validación de Token) y ole.guard.ts (Protección de módulos basada en la jerarquía de roles).
  - **Backend:** @EnableMethodSecurity y @PreAuthorize("hasRole('...')") en todos los endpoints, garantizando defensa en profundidad.

### 2.3. Asistente IA (Ruta B)
- Se decidió implementar un agente LLM nativo en Spring Boot que consume una API tipo OpenAI/Gemini (IA_API_URL).
- El frontend posee un Chat flotante (IaChatComponent) inyectado de forma global en MainLayoutComponent.
- Funciones acotadas: 1) Normativa, 2) Revisión de textos, 3) Generación de rúbricas.

### 2.4. Integridad de Capas
- Se eliminó el anti-patrón de inyectar Repositorios (IGestionConvocatoria, RequisitoAdjuntoRepository) directamente en los Controladores. Toda la lógica de negocio y consumo de DB pasa obligatoriamente por sus respectivos Servicios (IConvocatoriaService, IRequisitoAdjuntoService).

## 3. Próximos Pasos (Pendientes)
- Consolidar V1 (Base Schema) a partir del dump completo.
- Crear los Unit Tests para validación de SPs.
