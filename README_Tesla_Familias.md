# Tesla Familias

## Descripción del proyecto

**Tesla Familias** es una aplicación Android orientada a padres o apoderados de estudiantes de la Academia Preuniversitaria Tesla. Su objetivo es centralizar información relevante del estudiante y facilitar el acceso oportuno a datos como asistencias, tardanzas, inasistencias, obligaciones pendientes y notificaciones institucionales.

El proyecto se desarrollará como un **producto mínimo viable (MVP)** durante el término académico. La aplicación contará con un modo de demostración con datos locales para que pueda instalarse ya que será el prototipo de un proyecto real para los sistemas internos de la Academia Tesla con la cual estoy trabajando un sistema web llamado SIGAF.

## Exposición del problema

Actualmente, un padre puede necesitar ingresar a una plataforma web o esperar una comunicación institucional para conocer información importante relacionada con su hijo. Esto genera una demora entre el momento en que ocurre un evento, como una tardanza o inasistencia, y el momento en que el padre toma conocimiento.

Tesla Familias busca reducir esa brecha mediante una aplicación Android instalada en el dispositivo del padre, permitiendo consultar información de forma rápida y recibir notificaciones relevantes.

## Plataforma

La aplicación será desarrollada para **Android** utilizando:

- Kotlin
- Android Studio
- Arquitectura separada entre interfaz y capa de datos
- Datos locales de demostración para el MVP
- Preparación para consumo futuro de API REST
- Notificaciones Android
- Integración futura con Firebase Cloud Messaging (FCM)

Durante la evaluación académica, la aplicación podrá funcionar sin conexión al backend institucional mediante datos de prueba incluidos en el APK.

## Interfaz de usuario

La aplicación contará con las siguientes pantallas principales:

### Acceso
Permite al padre ingresar a la aplicación mediante credenciales de demostración.

### Inicio
Muestra un resumen del estudiante, estado de asistencia, obligaciones pendientes y notificaciones recientes.

### Asistencias
Permite consultar el historial de:
- Presencias
- Tardanzas
- Inasistencias

### Obligaciones
Muestra las obligaciones económicas pendientes del estudiante. El MVP será únicamente informativo y no procesará pagos.

### Notificaciones
Mantiene un historial de avisos recibidos, aunque estos hayan sido eliminados de la barra de notificaciones del dispositivo.

### Perfil
Muestra información básica del padre y del estudiante asociado.

## Interfaz de administración

Para fines de demostración, el MVP incluirá una interfaz administrativa mínima que permitirá:
- Cambiar el estado de asistencia de un estudiante de prueba.
- Crear una notificación de demostración.
- Modificar una obligación pendiente de prueba.
- Restablecer los datos del entorno de demostración.

Esta interfaz no representa el sistema administrativo real de Academia Tesla. Su finalidad es permitir que el evaluador pruebe los flujos principales sin depender de infraestructura externa.

## Funcionalidades del MVP

El alcance del proyecto incluye:

1. Inicio de sesión de demostración.
2. Persistencia de sesión.
3. Visualización de información del estudiante.
4. Consulta del historial de asistencias.
5. Consulta de obligaciones pendientes.
6. Historial de notificaciones.
7. Generación de notificaciones Android de demostración.
8. Funcionamiento con datos locales sin necesidad de backend.
9. Preparación de la capa de datos para futura conexión con endpoints REST.

## Funcionalidades fuera del alcance

Para mantener un alcance realista durante el término académico, no se incluirán inicialmente:

- Pagos en línea.
- Chat con profesores.
- Calificaciones.
- Matrícula.
- Geolocalización.
- Biometría.
- Administración completa de usuarios.
- Integración obligatoria con el ERP institucional.

Estas funcionalidades podrán considerarse como mejoras posteriores.

## Flujo principal

```text
Acceso
  ↓
Inicio
  ├── Asistencias
  ├── Obligaciones
  ├── Notificaciones
  └── Perfil
```

## Arquitectura propuesta

```text
Interfaz Android
      ↓
ViewModel / lógica de presentación
      ↓
Repositorio de datos
   ↙         ↘
Datos      API REST
locales    futura
```

El uso de un repositorio de datos permitirá que el modo de demostración funcione con información local y que, posteriormente, esta fuente pueda reemplazarse por servicios reales de Academia Tesla.

## Datos de demostración

La aplicación incluirá un estudiante ficticio y registros simulados de:
- Asistencias
- Tardanzas
- Inasistencias
- Obligaciones pendientes
- Notificaciones

Esto permitirá que cualquier peersona pueda instalar y probar la aplicación sin acceso a información real de estudiantes.

## Entrega esperada

Al finalizar el proyecto se espera contar con:
- Aplicación Android funcional.
- APK instalable.
- Código fuente disponible en GitHub.
- README con documentación del proyecto.
- Wireframes y capturas de pantalla.
- Datos de prueba incluidos.
- Instrucciones para ejecutar y probar la aplicación.

## Estado actual

**Módulo 1 – Borrador del proyecto**

En esta etapa se ha definido:
- Problema a resolver.
- Alcance del MVP.
- Plataforma.
- Funcionalidades.
- Interfaces principales.
- Arquitectura inicial.
- Estrategia de demostración sin backend.

## Autor
ROYER ALEXANDER CHAVEZ MALAVER:
Proyecto académico desarrollado para el curso de Desarrollo de Aplicaciones Móviles.

## Referencias

Android Developers. (2026). *Prepare your app for release*. Google.  
https://developer.android.com/studio/publish/preparing

Android Developers. (2026). *UI layer*. Google.  
https://developer.android.com/topic/architecture/ui-layer

Firebase. (2026). *Get started with Firebase Cloud Messaging in Android apps*. Google.  
https://firebase.google.com/docs/cloud-messaging/android/get-started
