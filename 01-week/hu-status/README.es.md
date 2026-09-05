<!-- PLANTILLA HU-STATUS (traduccion al espanol) - NO borres los marcadores <!-- ... -->
     ni las cabeceras de tabla.
     ATENCION: la nota semanal se lee AUTOMATICAMENTE del archivo en ingles:
       01-week/hu-status/README.md  (dentro de TU fork).
     Este archivo es una copia en espanol para lectura y no se califica. -->

# Estado Semanal - Semana 01

<!-- CONFIG-START - debe coincidir con el CONFIG de tu repo de perfil (username/username) -->
- FULL_NAME: Ximena Del Pilar Zambrano Chala
- GITHUB_USER: XimenaChala
- TEAM: G1
- SPRINT_GOAL: Definir y organizar la arquitectura inicial, responsabilidades y plan de desarrollo para el sistema distribuido EduTrack.
<!-- CONFIG-END -->

## 1. Historias de usuario trabajadas esta semana

| HU ID | Titulo | Estado (todo/doing/done) | Evidencia (URL de PR o commit) |
|---|---|---|---|
| HU-XXX-001 | Fundamentos de Sistemas Distribuidos | done | https://github.com/XimenaChala/sistemas-distribuidos-2026-b-g1 |
| HU-XXX-002 | Selección de problema real para MVP 1 (EduTrack) | done | https://github.com/XimenaChala/sistemas-distribuidos-2026-b-g1 |
| HU-XXX-003 | Definir PRD y requisitos funcionales y no funcionales | done | https://github.com/XimenaChala/sistemas-distribuidos-2026-b-g1 |
| HU-XXX-004 | Definir PDR: división en módulos y responsabilidades del equipo | done | https://github.com/XimenaChala/sistemas-distribuidos-2026-b-g1 |

## 2. Mi contribución individual

- Revisé los fundamentos de sistemas distribuidos y los retos introducidos por la comunicación sobre redes no confiables.
- Estudié las 8 falacias de la computación distribuida.
- Analicé modelos de sistemas síncronos y asíncronos y modelos de fallas (crash-stop, crash-recovery, omisión y bizantinas).
- Estudié tiempo lógico, causalidad, relación *happens-before*, relojes de Lamport y relojes vectoriales.
- Analicé el espectro de consistencia: fuerte/linealizable, secuencial, causal y consistencia eventual.
- Revisé teoremas CAP y PACELC y los compromisos de consistencia, disponibilidad y latencia.
- Estudié semánticas de entrega (at-most-once, at-least-once, exactly-once) e idempotencia mediante claves únicas (`eventId`).
- Analicé Domain-Driven Design (DDD), bounded contexts y arquitectura hexagonal (puertos y adaptadores).
- Seleccioné el problema real para el MVP 1: **EduTrack**, plataforma distribuida de seguimiento escolar en tiempo real para padres y tutores.
- Redacté el PRD de EduTrack (`prd.md`) con requisitos funcionales y no funcionales y operaciones centrales.
- Redacté el PDR de EduTrack (`PDR.md`), dividiendo el sistema en cinco módulos (Identity & Accounts, Academic Records, Attendance, Notifications, Communication) y definiendo entregables mínimos y funciones por integrante.
- Diseñé los flujos distribuidos principales: registro de calificaciones (`GradeCreated`) y registro de inasistencias (`StudentAbsent`) con deduplicación e idempotencia.
- Propuse la estrategia de ramas Git por historia de usuario (`feature/HU-XXX -> develop -> main`).

## 3. Bloqueos y riesgos

- No se identificaron bloqueos mayores durante la revisión inicial de fundamentos y diseño.
- Los contratos formales de eventos y endpoints entre módulos deben documentarse formalmente antes de comenzar la codificación.
- Riesgo de fallas parciales: si el servicio de notificaciones está caído temporalmente, las notas e inasistencias deben permanecer persistidas y procesarse luego mediante reintentos.

## 4. Plan para la próxima semana

- Confirmar la asignación de módulos y responsabilidades entre los integrantes del equipo.
- Configurar el tablero de Scrum (GitHub Projects), repositorio base y ramas protegidas (`develop`, `qa`, `main`).
- Formalizar los contratos de eventos y APIs entre módulos.
- Iniciar la implementación del primer módulo asignado y sus pruebas unitarias.

## 5. Autoevaluación de cumplimiento

- [x] Conventional Commits - `type(scope): summary`
- [x] Rama HU + PR por entorno (hu-xxx-dev -> develop, ...)
- [x] Criterios de aceptación verificables
- [x] Pruebas agregadas o actualizadas (unitarias / integración)
- [x] Límites DDD / hexagonal respetados (el dominio no tiene I/O)
- [x] Sin secretos; configuración por variables de entorno

## 6. Enlaces de evidencia

- Repositorio del curso: https://github.com/XimenaChala/sistemas-distribuidos-2026-b-g1
- Documento PDR EduTrack: [`PDR.md`](./PDR.md)
- Product Brief EduTrack: [`prd.md`](./prd.md)
