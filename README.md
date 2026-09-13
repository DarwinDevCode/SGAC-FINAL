
SGAC — Sistema de Gestión de Ayudantías de Cátedra

Documento de Contexto para Implementación

Universidad Técnica Estatal de Quevedo — UTEQ
Facultad de Ciencias de la Computación y Diseño Digital

Análisis profundo de usuarios, módulos, funcionalidades y flujos
 
1. Resumen Ejecutivo del Sistema
El SGAC (Sistema de Gestión de Ayudantías de Cátedra) es una aplicación web institucional diseñada para digitalizar, automatizar y centralizar el proceso completo de ayudantías de cátedra en la UTEQ. Reemplaza el flujo manual actual (correos, hojas de cálculo y documentos físicos) con un ciclo digital trazable de extremo a extremo.

Problema que resuelve
El proceso actual de gestión de ayudantías opera sobre correos y archivos dispersos. Esto genera:
•	Demoras en cada etapa del proceso (convocatoria, selección, seguimiento)
•	Duplicidad de trabajo al copiar datos entre documentos
•	Errores de transcripción en actas e informes redactados a mano
•	Nula trazabilidad — imposible saber en qué estado está una postulación
•	Carga administrativa excesiva para coordinadores y decanos
•	Falta de comunicación centralizada entre ayudantes, docentes y coordinación

Propuesta de valor
•	Ciclo completo en una sola plataforma: convocatoria → postulación → selección → ejecución → cierre
•	Validación automática de requisitos académicos vía integración con el SGA (solo lectura)
•	Generación automática de documentos oficiales: actas, resoluciones, certificados
•	Firma electrónica con validez legal para todos los documentos
•	Asistente de IA para guía operativa, generación de borradores y alertas de plazos
•	Control de acceso por roles: cada usuario ve y puede hacer solo lo que le corresponde
•	Notificaciones automáticas en tiempo real y por correo institucional

2. Arquitectura del Sistema — Módulos
El SGAC está organizado en 5 módulos funcionales que representan las etapas del proceso de ayudantías, más 1 módulo transversal de administración:

Módulo	Nombre	Descripción
M1	Postulación	Gestiona el ciclo de convocatoria y registro de postulantes. El estudiante consulta convocatorias, completa su expediente, sube documentos y envía su postulación.
M2	Evaluación y Selección	Implementa el concurso de méritos y oposición. La comisión califica, el sistema calcula rankings, resuelve empates y genera actas oficiales.
M3	Ejecución de Ayudantías	Cubre el trabajo diario del ayudante: plan de actividades, registro de sesiones, comunicación con docente y seguimiento de horas.
M4	Cierre del Proceso	Gestiona la etapa final: informe final, validaciones en cascada (docente → coordinador → decano) y emisión del certificado con firma digital.
M5	Administración	Panel de control del sistema: usuarios, roles, plantillas, criterios, configuración de etapas, auditoría y reportes ejecutivos.

3. Actores del Sistema — Perfiles de Usuario
El SGAC tiene 6 tipos de usuarios con roles claramente diferenciados. Cada actor tiene acceso exclusivamente a las funciones que le corresponden según su rol en el proceso de ayudantías.

3.1 Estudiante / Ayudante de Cátedra

👤  Estudiante / Ayudante de Cátedra
Usuario que postula y, si es seleccionado, ejecuta la ayudantía

¿Quién es?
Es el estudiante universitario que tiene buen desempeño académico y desea participar como ayudante de cátedra. Existen dos estados para este actor:
•	Estudiante postulante: antes de ser seleccionado, solo tiene acceso al módulo de postulación.
•	Ayudante de cátedra activo: una vez seleccionado, tiene acceso completo al módulo de ejecución.

¿Qué puede hacer en el sistema?
Módulo 1 — Postulación
1	Consultar las convocatorias vigentes publicadas por la coordinación (nombre de asignatura, requisitos, plazos, cupos disponibles).
2	Iniciar su postulación seleccionando la convocatoria de interés. El sistema crea automáticamente su expediente con un ID único.
3	Completar el formulario de postulación con sus datos personales, carrera y asignatura a la que postula.
4	Subir los documentos requeridos: certificado de no sanción, carta de recomendación del docente, y horario con disponibilidad mínima de 20h semanales.
5	Consultar el estado de su validación académica automática (el sistema verifica contra el SGA que sea estudiante regular, tenga nota ≥8 en la asignatura y promedio superior al de su carrera).
6	Enviar la postulación oficialmente una vez que todos los documentos estén cargados y las validaciones académicas pasen.
7	Consultar en tiempo real el estado de su postulación: 'En elaboración', 'En revisión', 'Aprobado', 'Rechazado', con las observaciones del coordinador.
8	Recibir notificaciones automáticas por correo institucional y dentro del sistema sobre cambios de estado.

Módulo 2 — Evaluación (acceso limitado como postulante)
1	Cargar el material didáctico (presentación, documentos) para su prueba de oposición, una vez que tenga asignado un tema y una fecha.
2	Consultar el tema del sílabo que le fue sorteado para la prueba de oposición.
3	Ver la fecha y hora programada para su prueba de oposición (citación con al menos 7 días de antelación).
4	Recibir notificación del resultado final (aprobado/rechazado) una vez que el proceso de selección concluya y los documentos estén firmados.

Módulo 3 — Ejecución (solo si fue seleccionado como ayudante)
1	Consultar su asignación formal: materia, docente responsable y período académico.
2	Subir su plan de actividades (semanal o mensual) para aprobación del docente. El sistema valida que no supere las 20h semanales.
3	Acceder al repositorio de documentos: reglamento, cronograma, formatos institucionales, rúbricas.
4	Registrar cada sesión de ayudantía indicando: fecha, tema impartido, lista de asistencia de estudiantes, tareas realizadas y evidencias (fotos, capturas, materiales).
5	Usar el canal interno de mensajería para comunicarse con el docente o la coordinación (los mensajes quedan archivados como evidencia institucional).
6	Recibir y visualizar la retroalimentación del docente sobre cada sesión registrada.
7	Generar y enviar los informes mensuales con el resumen de sesiones y evidencias.

Módulo 4 — Cierre
1	Subir el informe final de ayudantía (resumen completo del período) para iniciar el proceso de validación.
2	Consultar el estado del informe final: 'En revisión', 'Observado', 'Aprobado'.
3	Corregir y reenviar el informe en caso de que reciba observaciones del coordinador.
4	Descargar su certificado de participación una vez que el informe sea aprobado por toda la cadena (docente → coordinador → decano) y se firme el acta de cierre.

¿Cuándo puede hacer cada cosa?
Acción	Condición / Cuándo
Consultar convocatorias	En cualquier momento mientras haya sesión activa. No requiere ninguna condición previa.
Iniciar postulación	Solo si existe una convocatoria vigente con período de inscripción abierto y no tiene ya una postulación activa a esa misma convocatoria.
Subir documentos	Solo después de crear el expediente de postulación y mientras el período de inscripción esté abierto.
Enviar postulación	Solo cuando: (1) los documentos estén cargados Y (2) la validación académica del SGA diga 'Cumple'. Si alguna falla, el sistema bloquea el envío.
Ver estado de postulación	En cualquier momento después de haber enviado la postulación. Se actualiza en tiempo real.
Cargar material de oposición	Solo si tiene un tema asignado y existe una fecha programada para su prueba.
Registrar sesiones de ayudantía	Solo si ha sido seleccionado y tiene un plan de actividades aprobado y una asignación activa.
Enviar informe mensual	Solo dentro del período activo de ayudantía, una vez concluido el mes correspondiente.
Subir informe final	Solo al término del período de ayudantía, cuando la coordinación habilite el cierre.
Descargar certificado	Solo cuando el informe final sea aprobado por docente + coordinador + decano y el acta de cierre esté firmada digitalmente.


3.2 Docente

👤  Docente
Docente responsable de la asignatura que supervisa la ayudantía

¿Quién es?
El docente es el responsable académico de la asignatura a la que se asigna el ayudante. Su rol principal es supervisar, evaluar y retroalimentar el trabajo del ayudante durante la ejecución de la ayudantía. También participa en el inicio del proceso emitiendo el aval del postulante.

¿Qué puede hacer en el sistema?
Durante la Postulación (M1)
1	Emitir el aval del postulante: el docente confirma que el estudiante tiene su respaldo para participar como ayudante de su asignatura.
2	Dejar comentarios de mejora sobre la postulación si se requiere algún ajuste antes de la revisión formal.

Durante la Ejecución (M3)
1	Consultar y visualizar las actividades registradas por el ayudante: fechas, temas impartidos, asistencias y evidencias de cada sesión.
2	Enviar retroalimentación y observaciones al ayudante sobre el manejo de las sesiones, directamente vinculadas a cada sesión registrada.
3	Usar el canal interno de mensajería para comunicarse con el ayudante y la coordinación.
4	Recibir alertas automáticas cuando el ayudante registre una sesión sin evidencia adjunta.
5	Revisar y aprobar los informes mensuales del ayudante. Sin la aprobación del docente, el informe no avanza en el flujo.

Durante el Cierre (M4)
1	Revisar el informe final del ayudante y emitir su decisión: 'Aprobar' o 'Observar' (solicitar correcciones).
2	Si aprueba el informe final, este avanza automáticamente al coordinador para su revisión.
3	Comunicar incidencias graves (inasistencias repetidas, incumplimientos) a la coordinación para que gestione un reemplazo.

¿Cuándo puede hacer cada cosa?
Acción	Condición / Cuándo
Emitir aval del postulante	Durante el período de postulación abierto, cuando el estudiante selecciona su asignatura y el docente asociado.
Ver actividades del ayudante	Solo si tiene un ayudante asignado a su asignatura y en el período académico activo.
Enviar retroalimentación	Solo si el ayudante tiene al menos una sesión registrada. No puede enviar retroalimentación en el vacío.
Aprobar/observar informe mensual	Cuando el ayudante envíe el informe mensual. El docente tiene la primera instancia de revisión.
Aprobar/observar informe final	Cuando el ayudante envíe el informe final. Si lo observa, el proceso regresa al ayudante para correcciones.
Reportar incidencias	En cualquier momento durante el período de ejecución activo.


3.3 Coordinador de Carrera

👤  Coordinador de Carrera
Gestor principal del proceso de ayudantías en la facultad

¿Quién es?
El coordinador es el actor con mayor carga operativa en el sistema. Es responsable de gestionar todo el ciclo: desde publicar la convocatoria hasta validar los informes finales. Actúa como eje central que conecta a los estudiantes, docentes, la comisión de selección y el decanato.

¿Qué puede hacer en el sistema?
Módulo 1 — Postulación
1	Crear y publicar convocatorias: define la asignatura, número de cupos, requisitos, plazos y publica en el sistema (notificación automática a destinatarios).
2	Gestionar el cronograma normado: el sistema asegura que la convocatoria inicie en la primera semana, el período de inscripción dure 5 días y los resultados se publiquen en 3 días.
3	Revisar y decidir sobre los documentos cargados por cada postulante: aprobación, rechazo con comentarios registrados.
4	Ver en tiempo real cuántas postulaciones hay, cuáles están completas, cuáles tienen problemas de documentación.
5	Notificar a los postulantes sobre decisiones de sus documentos (el sistema envía automáticamente al cambiar el estado).

Módulo 2 — Evaluación y Selección
1	Organizar y gestionar el concurso de méritos y oposición: definir el tribunal evaluador, programar la agenda y enviar citaciones (con al menos 7 días de antelación).
2	Cargar los temas del sílabo disponibles para el sorteo de la prueba de oposición.
3	Publicar las fechas de evaluación asignando fecha, hora y tema a cada postulante, con notificación automática.
4	Revisar el orden de mérito generado automáticamente por el sistema y validar los resultados finales.
5	Declarar a los ganadores una vez revisado el ranking y aprobar la lista para publicación.
6	Publicar los resultados del concurso (todos los postulantes son notificados automáticamente).
7	Generar y firmar digitalmente las actas de resultados y las resoluciones de designación.

Módulo 3 — Ejecución
1	Formalizar la asignación del ayudante seleccionado a su materia, docente y período académico.
2	Revisar el plan de actividades subido por el ayudante y aprobarlo o pedir ajustes.
3	Monitorear el progreso de cada ayudantía: sesiones registradas, horas acumuladas, evidencias subidas.
4	Revisar y aprobar los informes mensuales de los ayudantes (segunda instancia, después del docente).
5	Gestionar incidencias: registrar inasistencias o incumplimientos, y ejecutar reemplazos usando el ranking del concurso.

Módulo 4 — Cierre
1	Revisar el informe final del ayudante (segunda validación, después del docente).
2	Aprobar u observar el informe final. Si hay observaciones, el proceso regresa al ayudante.
3	Generar el borrador del acta de cierre una vez que el informe esté aprobado.
4	Firmar digitalmente el acta de cierre con su token o clave digital.
5	El sistema habilita automáticamente la generación del certificado del ayudante después del cierre.


3.4 Comisión de Selección (Tribunal)

👤  Comisión de Selección / Tribunal
Evaluadores del concurso de méritos y oposición

¿Quién es?
La comisión de selección es el órgano evaluador que juzga el concurso de méritos y oposición. Está integrada por miembros designados por la institución para evaluar de manera objetiva a los postulantes. Su actividad en el sistema está concentrada exclusivamente en el Módulo 2.

¿Qué puede hacer en el sistema?
1	Revisar los expedientes de los postulantes que pasaron la revisión inicial de documentos por el coordinador.
2	Verificar la elegibilidad de cada postulante: revisar que cumpla todos los requisitos académicos y documentales.
3	Registrar manualmente los puntajes de méritos académicos: experiencia previa como ayudante (máx. 4pts) y participación en eventos académicos (máx. 2pts). El sistema ya tiene calculados automáticamente los puntos por notas en la asignatura (máx. 10pts) y promedio (máx. 4pts).
4	Iniciar el sorteo del tema de oposición: el sistema selecciona aleatoriamente un tema del sílabo para cada postulante, con evidencia y sello de tiempo.
5	Conducir la prueba de oposición e ingresar los puntajes por cada uno de los 3 criterios: material didáctico (máx. 10pts), calidad de la exposición (máx. 4pts) y pertinencia de las respuestas (máx. 6pts).
6	Aplicar criterios de desempate cuando dos postulantes empaten en puntaje total (el sistema aplica automáticamente: mayor puntaje en oposición > mayor puntaje en conocimientos > mayor puntaje en respuestas).
7	Generar y firmar digitalmente las actas de resultados de méritos y oposición.

Reglas de negocio clave para la Comisión
Criterios y puntajes del concurso (total 40 puntos)
•	MÉRITOS (máx. 20 pts): Nota en la asignatura ≤10pts + Promedio general ≤4pts + Experiencia previa ≤4pts + Eventos académicos ≤2pts
•	OPOSICIÓN (máx. 20 pts): Material didáctico ≤10pts + Calidad exposición ≤4pts + Pertinencia respuestas ≤6pts
•	MÍNIMO PARA ELEGIBILIDAD: El postulante debe obtener ≥25/40 puntos totales para ser considerado elegible.
•	DESEMPATE: Mayor puntaje en oposición → Mayor puntaje en conocimientos → Mayor pertinencia de respuestas.
•	CITACIÓN: La prueba de oposición debe notificarse con al menos 7 días de anticipación.


3.5 Decano (o Delegado)

👤  Decano / Delegado del Decanato
Autoridad máxima con función aprobatoria y de firma institucional

¿Quién es?
El decano preside la facultad y tiene el nivel de autoridad más alto dentro del proceso. Su rol en el sistema es principalmente aprobatorio: valida lo actuado por el tribunal y la coordinación, firma resoluciones y autoriza la emisión de certificados. También consulta reportes ejecutivos para toma de decisiones.

¿Qué puede hacer en el sistema?
1	Revisar y aprobar lo actuado por el tribunal en el concurso de selección: verificar que el proceso fue correcto y los resultados son válidos.
2	Firmar digitalmente las resoluciones de designación de los ayudantes seleccionados, dándoles validez legal oficial.
3	Revisar el informe final del ayudante (tercera y última instancia de validación, después del docente y coordinador).
4	Aprobar la emisión de certificados: sin su aprobación, el sistema no genera el certificado del ayudante.
5	Consultar reportes ejecutivos del proceso: número de postulaciones, tiempos por fase, horas ejecutadas, cumplimiento de criterios, resultados por carrera.
6	Atender incidencias que requieran decisión de autoridad superior.
7	Consultar el historial completo de ayudantías de cualquier período para auditoría.

¿Cuándo puede hacer cada cosa?
Acción	Condición / Cuándo
Revisar y aprobar resultados del tribunal	Solo después de que el tribunal haya completado la evaluación de méritos y oposición y se hayan generado las actas.
Firmar resolución de designación	Solo después de aprobar los resultados. El sistema requiere que el coordinador también haya firmado primero.
Revisar informe final del ayudante	Solo después de que el docente y coordinador lo hayan aprobado. Si lo observa, vuelve al ayudante.
Autorizar emisión de certificados	Solo cuando el informe final está aprobado en todas las instancias y el acta de cierre está firmada.
Consultar reportes ejecutivos	En cualquier momento, para cualquier período académico con datos registrados.

3.6 Administrador del Sistema

👤  Administrador del Sistema
Usuario técnico-operativo que mantiene la configuración del sistema

¿Quién es?
El administrador es el usuario técnico-operativo del sistema. No participa en el proceso académico sino en la configuración y mantenimiento del sistema. Es el único que puede crear usuarios, asignar roles, definir plantillas y ajustar la configuración global.

¿Qué puede hacer en el sistema?
1	Gestionar usuarios: crear, editar, desactivar cuentas y asignar/revocar roles a cualquier actor del sistema.
2	Mantener plantillas oficiales: convocatorias, actas de méritos, actas de oposición, informes mensuales, informes finales y resoluciones.
3	Configurar criterios y rúbricas de evaluación publicadas para el concurso de selección.
4	Configurar las etapas del proceso: habilitar/deshabilitar módulos, definir plazos globales y gestionar calendarios.
5	Configurar y monitorear la sincronización en solo lectura con el SGA (importar datos de estudiantes y asignaturas).
6	Gestionar el servicio de notificaciones: configurar plantillas de mensajes, canales de envío y registros de notificaciones.
7	Consultar el registro de auditoría completo: qué hizo cada usuario, cuándo y desde qué acción.
8	Gestionar respaldos de la base de datos y atender incidencias operativas.
9	Custodiar la privacidad de los datos conforme a la Ley de Protección de Datos Personales (LOPDP).


4. Flujos Detallados por Módulo

4.1 Módulo 1 — Postulación
Este módulo cubre desde la publicación de la convocatoria hasta el envío formal de la postulación por el estudiante.

Flujo completo del proceso de postulación
#	Actor	Acción / Evento
1	Coordinador	Crea la convocatoria: define asignatura, cupos, requisitos, plazos y la publica en el sistema.
2	Sistema	Notifica automáticamente a todos los estudiantes elegibles por correo institucional y en el panel.
3	Estudiante	Consulta las convocatorias vigentes y selecciona la que le interesa.
4	Sistema	Crea el expediente de postulación con ID único, estado 'En elaboración', e inicializa el checklist de documentos.
5	Sistema	Lanza automáticamente la validación académica contra el SGA: verifica matrícula regular, nota ≥8 en asignatura, promedio superior al de carrera.
6	Estudiante	Completa el formulario con sus datos y sube los 3 documentos: certificado de no sanción, carta de recomendación y horario con ≥20h disponibles.
7	Estudiante	Hace clic en 'Postular'. El sistema verifica que todas las validaciones pasen y todos los documentos estén cargados.
8	Sistema	Cambia estado a 'En revisión', notifica al coordinador y bloquea la edición del expediente.
9	Coordinador	Revisa los documentos manualmente. Puede aprobar o rechazar con observaciones.
10	Sistema	Notifica al estudiante el resultado de la revisión documental. Si hay observaciones, el estudiante puede corregir y reenviar.
11	Coordinador	Marca el expediente como 'Apto' para pasar a la siguiente etapa del concurso.

Estados posibles de una postulación
Estado	Significado
En elaboración	El estudiante está completando el formulario y cargando documentos. Aún no ha enviado.
En revisión	El estudiante envió la postulación. Está esperando que el coordinador revise los documentos.
Observado	El coordinador encontró problemas en los documentos. El estudiante debe corregir y reenviar.
Apto	La documentación fue aprobada. El postulante puede participar en el concurso de selección.
No apto	El postulante no cumple los requisitos académicos o documentales. El proceso finaliza aquí.
Seleccionado	El postulante ganó el concurso de selección. Pasa a la fase de ejecución.
No seleccionado	Participó en el concurso pero no obtuvo el puesto. Queda en el ranking por si hay reemplazos.


4.2 Módulo 2 — Evaluación y Selección
Este módulo implementa el concurso de méritos y oposición para seleccionar al ayudante de cada asignatura. Es el módulo más complejo en términos de reglas de negocio.

Flujo del concurso de méritos y oposición
#	Actor	Acción / Evento
1	Sistema	Calcula automáticamente los puntos de méritos académicos de cada postulante consultando el SGA (notas y promedios).
2	Comisión	Revisa los expedientes de postulantes 'Aptos' y registra manualmente los puntajes de experiencia y eventos académicos.
3	Comisión	Inicia el sorteo de temas: el sistema asigna aleatoriamente un tema del sílabo a cada postulante con sello de tiempo.
4	Coordinador	Carga el cronograma de la prueba de oposición (fecha, hora, sala o enlace virtual) y el sistema notifica a cada postulante.
5	Estudiante	Carga el material didáctico (presentación) para su prueba en el plazo indicado.
6	Comisión	Conduce la prueba de oposición e ingresa los puntajes por cada criterio: material (máx.10), exposición (máx.4), respuestas (máx.6).
7	Sistema	Suma méritos + oposición para cada postulante y genera el orden de mérito automáticamente.
8	Sistema	Aplica desempates si hay empates: oposición → conocimientos → respuestas. Marca elegibles (≥25/40pts).
9	Coordinador	Revisa el ranking generado y valida los resultados. Si todo está correcto, aprueba la lista.
10	Sistema	Genera automáticamente el acta de resultados y la resolución de designación del ganador.
11	Coordinador + Decano	Firman digitalmente los documentos con su token/clave institucional. El sistema registra la firma con sello de tiempo.
12	Sistema	Publica los resultados y notifica a TODOS los postulantes: ganadores y no seleccionados.


4.3 Módulo 3 — Ejecución de Ayudantías
Este módulo cubre el trabajo diario del ayudante durante el período académico. Es el módulo con mayor duración temporal y más interacciones continuas.

Flujo semanal típico de ejecución
#	Actor	Acción / Evento
1	Coordinador	Al inicio del período: formaliza la asignación del ayudante a su materia, docente y período académico.
2	Ayudante	Sube el plan de actividades (semanal/mensual). El sistema valida que no supere 20h/semana.
3	Docente / Coordinador	Revisa y aprueba el plan de actividades. Si hay ajustes, los comunica por el canal de mensajería.
4	Ayudante	Realiza la sesión de ayudantía con los estudiantes.
5	Ayudante	Registra la sesión en el sistema: fecha, tema, asistencia de estudiantes, tareas realizadas y evidencias adjuntas.
6	Sistema	Suma las horas de la sesión al contador del período. Alerta si se acerca al límite de 20h/semana.
7	Docente	Consulta las actividades registradas y envía retroalimentación al ayudante si corresponde.
8	Ayudante	Al cierre del mes: genera y envía el informe mensual (resumen de sesiones, horas, evidencias).
9	Docente	Revisa y aprueba el informe mensual (1ª instancia).
10	Coordinador	Revisa y aprueba el informe mensual (2ª instancia). El ciclo mensual se repite.

4.4 Módulo 4 — Cierre del Proceso
El módulo de cierre gestiona la etapa final con una cadena de validaciones secuenciales (docente → coordinador → decano) antes de emitir el certificado.

Flujo de cierre y certificación
1	El ayudante sube el informe final completo con el resumen del período completo de ayudantía.
2	El sistema cambia el estado a 'En revisión' y notifica al docente.
3	El docente revisa el informe final. Opciones: (A) Aprobarlo → avanza al coordinador; (B) Observarlo → vuelve al ayudante para correcciones.
4	Si el docente aprueba, el coordinador recibe notificación y revisa el informe. Mismas opciones: aprobar o observar.
5	Si el coordinador aprueba, el decano recibe notificación. El decano revisa y aprueba (última instancia).
6	Con las 3 aprobaciones completas, el coordinador genera el borrador del acta de cierre.
7	El coordinador firma digitalmente el acta de cierre. El sistema registra la firma y sello de tiempo.
8	El sistema cambia el estado del proceso a 'Cerrado', archiva el acta firmada y habilita la generación del certificado.
9	El decano autoriza la emisión del certificado. El sistema genera el certificado oficial con firma digital.
10	El ayudante recibe notificación y puede descargar su certificado de participación desde su panel.


5. Matriz de Permisos por Actor y Módulo
La siguiente tabla resume qué puede hacer cada actor en cada módulo. Esta es la matriz de referencia para implementar el control de acceso por roles.

Funcionalidad	Estudiante	Docente	Coordinador	Comisión	Decano
[M1] Consultar convocatorias	✓	✓	✓	✓	✓
[M1] Crear/publicar convocatoria	✗	✗	✓	✗	✗
[M1] Postularse a convocatoria	✓	✗	✗	✗	✗
[M1] Subir documentos propios	✓	✗	✗	✗	✗
[M1] Emitir aval de postulante	✗	✓	✗	✗	✗
[M1] Revisar/aprobar documentos	✗	✗	✓	✗	✗
[M1] Ver estado de postulación	✓ propio	✓ avales	✓ todos	~	✓
[M2] Calcular méritos automáticos	✗	✗	✗	✗	✗
[M2] Ingresar puntajes méritos manuales	✗	✗	✗	✓	✗
[M2] Sortear tema de oposición	✗	✗	✗	✓	✗
[M2] Cargar material de oposición	✓ propio	✗	✗	✗	✗
[M2] Ingresar puntajes oposición	✗	✗	✗	✓	✗
[M2] Ver ranking / orden de mérito	✗	✗	✓	✓	✓
[M2] Declarar ganadores y publicar	✗	✗	✓	✗	✗
[M2] Firmar actas y resoluciones	✗	✗	✓	✓ actas	✓ resol.
[M2] Aprobar resultados del tribunal	✗	✗	✗	✗	✓
[M3] Formalizar asignación ayudante	✗	✗	✓	✗	✗
[M3] Subir plan de actividades	✓	✗	✗	✗	✗
[M3] Aprobar plan de actividades	✗	✓	✓	✗	✗
[M3] Registrar sesiones	✓	✗	✗	✗	✗
[M3] Consultar actividades del ayudante	✓ propio	✓ asign.	✓ todos	✗	✓
[M3] Enviar retroalimentación	✗	✓	✓	✗	✗
[M3] Usar mensajería interna	✓	✓	✓	✗	~
[M3] Aprobar informes mensuales	✗	✓ 1ª inst.	✓ 2ª inst.	✗	✗
[M3] Gestionar incidencias/reemplazos	✗	✗	✓	✗	✗
[M4] Subir informe final	✓	✗	✗	✗	✗
[M4] Aprobar informe final	✗	✓ 1ª	✓ 2ª	✗	✓ 3ª
[M4] Generar y firmar acta de cierre	✗	✗	✓	✗	✗
[M4] Autorizar emisión de certificado	✗	✗	✗	✗	✓
[M4] Descargar certificado propio	✓	✗	✗	✗	✗
[M5] Gestionar usuarios y roles	✗	✗	✗	✗	✗
[M5] Configurar plantillas y criterios	✗	✗	✗	✗	✗
[M5] Consultar auditoría completa	✗	✗	✗	✗	✓
[M5] Sincronizar SGA	✗	✗	✗	✗	✗
[M5] Ver reportes ejecutivos	✗	✗	✓	✗	✓
Leyenda: ✓ = Puede hacer, ✗ = No puede hacer, ~ = Acceso parcial o de solo lectura. El Administrador del Sistema tiene acceso total al Módulo 5 y acceso de solo lectura al resto.


6. Reglas de Negocio Críticas
Las siguientes reglas son las restricciones y validaciones que el sistema debe aplicar de forma automática e irrestricta, derivadas del Reglamento de Ayudantías de la UTEQ.

Reglas de Postulación
•	El estudiante debe ser estudiante regular (matrícula vigente verificada en SGA).
•	Nota mínima de 8/10 en la asignatura a la que postula (verificado en SGA).
•	Promedio superior al promedio de la carrera en el período anterior (verificado en SGA).
•	Disponibilidad mínima de 20 horas semanales demostrada en el horario adjunto.
•	Un estudiante NO puede postular a la misma convocatoria más de una vez.
•	El sistema bloquea el envío si alguna validación del SGA no pasa.

Reglas del Concurso de Selección
•	Méritos máximo: 20 puntos (notas 10 + promedio 4 + experiencia 4 + eventos 2).
•	Oposición máximo: 20 puntos (material 10 + exposición 4 + respuestas 6).
•	Mínimo para ser elegible: 25 de 40 puntos totales.
•	La citación para la prueba de oposición debe hacerse con al menos 7 días de anticipación.
•	Desempate automático: mayor puntaje oposición > mayor puntaje conocimientos > mayor pertinencia respuestas.
•	Los temas del sorteo deben ser del sílabo oficial de la asignatura.
•	Las actas deben ser firmadas digitalmente por la comisión, coordinador y decano para tener validez.

Reglas de Ejecución
•	El ayudante NO puede planificar más de 20 horas semanales (el sistema rechaza sobreasignaciones automáticamente).
•	El sistema alerta si una sesión se registra sin evidencia adjunta.
•	El informe mensual debe ser aprobado por el docente ANTES de que lo vea el coordinador (flujo secuencial).
•	Si el ayudante incumple (inasistencias, incumplimientos graves), el coordinador puede iniciar el proceso de reemplazo usando el ranking del concurso.
•	El historial de ayudantías se conserva por al menos 2 años después del cierre del período.

Reglas de Cierre y Certificación
•	El certificado SOLO se genera cuando: (1) el informe final fue aprobado por docente + coordinador + decano, Y (2) el acta de cierre fue firmada digitalmente.
•	El flujo de aprobación del informe final es estrictamente secuencial: docente → coordinador → decano.
•	La IA solo puede generar borradores de documentos; la validación y firma siempre la realiza un humano.
•	Las firmas digitales deben ser emitidas por la Autoridad de Certificación Ecuador para tener validez legal.
•	El proceso de cierre de un período lo realiza el administrador con supervisión de la coordinación.


7. Sistemas Externos e Integraciones
El SGAC se integra con 4 sistemas externos. Estas integraciones son fundamentales para el funcionamiento del sistema y deben ser implementadas desde el inicio del desarrollo.

Sistema Externo	Descripción del uso e integración
SGA (Sistema de Gestión Académica)	Integración en SOLO LECTURA. El SGAC consulta al SGA para verificar: matrícula regular, nota del estudiante en la asignatura, promedio general, asignaturas aprobadas, carrera inscrita, y horarios. El SGA nunca recibe datos del SGAC — solo los provee. Esta integración es crítica para la validación automática de requisitos.
Sistema de Mensajería Institucional	El SGAC usa el servicio de correo institucional para enviar notificaciones automáticas a todos los actores: apertura de convocatorias, cambios de estado, resultados, citaciones, alertas de plazos. Todas las notificaciones quedan registradas (qué se envió, a quién, cuándo).
Servicio de IA (API externa)	Un servicio de IA (vía API) actúa como asistente dentro de la plataforma. Funciones: responder dudas frecuentes de los usuarios, sugerir y generar borradores de documentos (convocatorias, actas, informes) y alertar sobre plazos o datos faltantes. RESTRICCIÓN: solo se envían resúmenes del caso al servicio de IA, nunca datos sensibles. Toda salida de IA debe ser revisada y aprobada por un humano antes de usarse oficialmente.
Servicio de Firma Electrónica	Para dar validez legal a los documentos oficiales (actas, informes, resoluciones, certificados). Solo los roles autorizados pueden firmar: comisión, coordinación y decanato. El documento firmado queda almacenado en el sistema con el certificado de firma. Las firmas deben ser emitidas por la Autoridad de Certificación Ecuador.


8. Catálogo de Requerimientos Funcionales
Lista completa de los 20 requerimientos funcionales del sistema, con su módulo, actor responsable y prioridad.

ID	Módulo	Funcionalidad	Actor	Prioridad
RF001	Postulación	Registro de postulaciones por estudiantes (formulario digital con validaciones)	Estudiante	Alta
RF002-A	Postulación	Validación automática de requisitos académicos vía SGA	Sistema / SGA	Alta
RF002-B	Postulación	Revisión manual de documentos adjuntos por coordinador	Coordinador	Alta
RF003	Postulación	Acceso centralizado a documentos por convocatoria (reglamentos, guías, rúbricas)	Todos	Media
RF004	Postulación	Consultas de estado en tiempo real + notificaciones automáticas	Sistema	Alta
RF005	Evaluación	Registro de puntajes de méritos y oposición con cálculo automático	Comisión / Sistema	Alta
RF006	Evaluación/Cierre	Generación automática de documentos oficiales (actas, resoluciones, certificados)	Sistema	Alta
RF007	Ejecución	Registro de sesiones de ayudantía (fecha, tema, asistencia, evidencias)	Ayudante	Alta
RF008	Ejecución	Canal interno de comunicación con historial y alertas	Ayudante, Docente, Coordinador	Alta
RF009	Ejecución	Evaluaciones periódicas de desempeño por docentes/coordinadores	Docente / Coordinador	Alta
RF010	Ejecución	Registro de informes mensuales con flujo de aprobación secuencial	Ayudante/Docente/Coord.	Alta
RF011	Cierre	Emisión automática de certificado tras aprobaciones completas	Sistema	Alta
RF012	Transversal	Historial de ayudantías por estudiante (asignatura, período, horas, desempeño)	Sistema	Alta
RF013	Postulación	Gestión de convocatorias con cronograma normado (5 días inscripción, 3 días publicación)	Coordinador	Alta
RF014	Evaluación	Registro de puntaje de méritos (desempeño ≤10, promedios ≤4, experiencia ≤4, eventos ≤2)	Comisión	Alta
RF015	Evaluación	Gestión de la oposición: sorteo, citación ≥7 días, puntajes manuales	Comisión / Coordinador	Alta
RF016	Evaluación	Consolidación de calificación final (≥25/40), desempates y publicación de resultados	Sistema	Alta
RF017	Ejecución	Registro de incidencias y reemplazos por ranking con notificaciones	Coordinador	Alta
RF018	Evaluación	Generación de reportes consolidados de participantes para aprobación	Sistema	Alta
RF019	Ejecución	Validación de planificación semanal ≤20h con rechazo automático de sobreasignaciones	Sistema	Alta
RF020	Transversal	Asistente de IA para informes personalizados y borradores de documentos	Sistema (IA)	Media


9. Requerimientos de Calidad (No Funcionales)
Atributos de calidad que el sistema debe cumplir, alineados con ISO/IEC 25010:2011.

Atributo (ISO 25010)	Descripción y criterio de aceptación
RC001 — Adecuación Funcional	Las funciones implementadas deben cubrir completamente las necesidades especificadas sin omisiones. Verificado por pruebas de cobertura funcional.
RC002 — Eficiencia de Rendimiento	Las operaciones deben responder en tiempos razonables incluso con carga concurrente. Las validaciones del SGA no deben bloquear la interfaz por más de 3 segundos.
RC003 — Compatibilidad	El sistema debe coexistir con el SGA y otros sistemas institucionales sin conflictos. Las integraciones deben funcionar sin errores de compatibilidad.
RC004 — Usabilidad	Los usuarios deben poder realizar sus tareas sin entrenamiento extenso. Interfaz clara para todos los perfiles (estudiantes, docentes, coordinadores, decano).
RC005 — Confiabilidad	El sistema debe recuperarse de fallos sin pérdida de datos. Respaldo periódico obligatorio con recuperación documentada.
RC006 — Seguridad	Control de acceso estricto por roles. Protección contra accesos no autorizados. Cumplimiento de la LOPDP (Ley Orgánica de Protección de Datos Personales del Ecuador).
RC007 — Mantenibilidad	El código debe permitir modificaciones y pruebas eficientes. Arquitectura que facilite agregar nuevas carreras o módulos.
RC008 — Portabilidad	El sistema debe poder desplegarse en distintos entornos sin cambios significativos, lo que facilita la expansión a otras facultades de la UTEQ.


10. Restricciones Técnicas y Legales
Limitaciones que el equipo de desarrollo debe respetar obligatoriamente al implementar el SGAC.

Restricción	Descripción
RES001 — Reglamento UTEQ	Todo el sistema debe cumplir el Reglamento de Ayudantías de la UTEQ: límites de horas, cronogramas, criterios de selección, desempates, obligaciones de las partes.
RES002 — Ley de Protección de Datos	Cumplimiento obligatorio de la LOPDP (Ley Orgánica de Protección de Datos Personales). Los datos sensibles de los estudiantes no pueden compartirse con terceros no autorizados.
RES003 — Tecnologías open-source	El sistema debe desarrollarse con tecnologías web compatibles con navegadores actuales y sin dependencia de software que requiera licencias de pago adicionales.
RES004 — Firmas digitales certificadas	Las firmas electrónicas de documentos oficiales deben ser emitidas por la Autoridad de Certificación Ecuador para tener validez legal.
RES005 — Infraestructura UTEQ	El sistema debe desplegarse en la infraestructura existente de la UTEQ. Cualquier ampliación de servidores requiere aprobación previa de la Dirección TIC.
RES006 — Límite de horas semanal	El sistema debe validar y rechazar automáticamente planificaciones que superen las 20 horas semanales del ayudante. No debe existir excepción a esta regla.
RES007 — ISO 12207	El ciclo de vida del desarrollo debe alinearse con la norma ISO/IEC 12207:2017, comenzando formalmente desde la fase de requisitos (ya documentada en este documento).
RES008 — IA solo como soporte	La IA no puede tomar decisiones vinculantes en el proceso. Solo genera borradores y sugerencias. La validación final siempre debe realizarla un usuario humano autorizado.


11. Glosario de Términos Clave

Término	Definición
Ayudantía de cátedra	Actividad institucional de apoyo académico realizada por un estudiante con buen rendimiento, que asiste al docente y apoya a sus pares en el proceso de aprendizaje.
SGAC	Sistema de Gestión de Ayudantías de Cátedra — el sistema documentado en este archivo.
SGA	Sistema de Gestión Académica — plataforma institucional de la UTEQ que contiene los datos académicos oficiales de los estudiantes (matrículas, notas, promedios).
Período académico	Intervalo semestral regulado por el Reglamento de Régimen Académico, durante el cual se ejecutan las ayudantías.
Convocatoria	Anuncio oficial publicado por la coordinación que invita a los estudiantes elegibles a postularse como ayudantes de una asignatura específica.
Expediente de postulación	Registro digital creado por el sistema cuando el estudiante inicia una postulación, que contiene todos sus datos, documentos y el historial de estados.
Concurso de méritos y oposición	Proceso de selección que combina la evaluación de méritos académicos (20pts) con una prueba de oposición (20pts) para seleccionar al mejor candidato.
Orden de mérito	Ranking generado automáticamente por el sistema con todos los postulantes ordenados de mayor a menor puntaje total, con la indicación de elegibles.
Acta	Documento oficial generado por el sistema que deja constancia del proceso de selección, los puntajes y los resultados. Requiere firma digital para ser válida.
Resolución de designación	Documento oficial firmado por coordinador y decano que designa formalmente al estudiante ganador como ayudante de cátedra.
Informe mensual	Reporte que el ayudante envía al finalizar cada mes con el resumen de sus sesiones, horas y evidencias. Requiere aprobación del docente y coordinador.
Informe final	Reporte integral que el ayudante envía al terminar el período completo de ayudantía. Requiere aprobación de docente, coordinador y decano.
Certificado de participación	Documento oficial emitido al cierre del proceso que acredita que el estudiante completó exitosamente su ayudantía. Solo se emite con todas las aprobaciones y el acta firmada.
RRA	Reglamento de Régimen Académico del Consejo de Educación Superior — norma nacional que regula las funciones académicas de las IES del Ecuador.
LOPDP	Ley Orgánica de Protección de Datos Personales del Ecuador — norma legal que regula el tratamiento de datos personales y que el SGAC debe cumplir.
Rúbrica	Instrumento con criterios explícitos y ponderaciones específicas para valorar el desempeño del postulante de forma objetiva y consistente.
Firma digital	Mecanismo de autenticación electrónica con validez legal, emitida por la Autoridad de Certificación Ecuador, usada para firmar los documentos oficiales del proceso.

Documento de análisis y contexto — SGAC v1.0
Basado en: Análisis, Estructuración y Modelado de Requerimientos del Sistema de Ayudantías de Cátedra de la UTEQ
