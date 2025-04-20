# Indexador Nexus

![Java](https://img.shields.io/badge/Java-17-orange) ![JavaFX](https://img.shields.io/badge/JavaFX-17-blue) ![Maven](https://img.shields.io/badge/Maven-3.8.6-red)

*[English version](README.en.md)*

## Descripción

Indexador para Argentum Online programado en Java. Esta herramienta permite visualizar, editar y gestionar los recursos gráficos utilizados en el juego Argentum Online. Actualmente trabaja con recursos de tipo 0.13 o AOLibre.

## Características

- Visualización de gráficos (GRHs)
- Edición de propiedades de gráficos
- Gestión de animaciones
- Sistema de caché para optimizar rendimiento
- Visualización de escudos, cascos y otras características del juego
- Sistema de logging centralizado
- Interfaz gráfica intuitiva con JavaFX

## Capturas de Pantalla

![Vista principal](https://github.com/Lorwik/Indexador-Nexus/assets/1338437/ea753277-4461-4e4e-a67d-8397823a2500)

![Editor de escudos](https://github.com/Lorwik/Indexador-Nexus/assets/1338437/05fb4355-b4eb-48fb-81a1-d97ddff02761)

![Editor de gráficos](https://github.com/Lorwik/Indexador-Nexus/assets/1338437/bff3274b-6dec-459a-8141-245c3754563e)

## Requisitos del Sistema

- Java Development Kit (JDK) 17 o superior
- Maven 3.6.0 o superior
- Recursos gráficos de Argentum Online versión 0.13 o AOLibre

## Instalación

### Opciones de Instalación

#### 1. Clonación del Repositorio (Desarrollo)

1. Clona el repositorio:
   ```bash
   git clone https://github.com/Lorwik/Indexador-Nexus.git
   ```

2. Navega al directorio del proyecto:
   ```bash
   cd Indexador-Nexus
   ```

3. Compila el proyecto usando Maven:
   ```bash
   mvn clean package
   ```

#### 2. Descarga Directa (Usuarios)

1. Descarga la última versión desde la [sección de Releases](https://github.com/Lorwik/Indexador-Nexus/releases)
2. Descomprime el archivo descargado en una ubicación de tu elección

## Ejecución de la Aplicación

### Desde Línea de Comandos

1. Navega hasta la carpeta del proyecto
2. Ejecuta el siguiente comando:
   ```bash
   mvn clean javafx:run
   ```

### Usando el Archivo JAR

1. Navega hasta la carpeta donde está el archivo JAR compilado (generalmente en `/target`)
2. Ejecuta el siguiente comando:
   ```bash
   java -jar indexador-1.0-SNAPSHOT.jar
   ```

### Configuración Inicial

Al iniciar la aplicación por primera vez:

1. Selecciona la ruta donde se encuentran los recursos de Argentum Online
2. La aplicación cargará automáticamente los gráficos disponibles
3. Utiliza la interfaz para navegar entre los diferentes recursos

## Estado del Proyecto

Este proyecto se encuentra en desarrollo activo. Algunas características planificadas son:

- Importación desde archivo de texto plano
- Optimización de rendimiento para grandes cantidades de gráficos
- Soporte para nuevos formatos de recursos

## Arquitectura

El proyecto está estructurado siguiendo el patrón Modelo-Vista-Controlador (MVC):

- **Modelo**: Clases de datos en `org.nexus.indexador.gamedata.models`
- **Vista**: Interfaces FXML en `resources/fxml`
- **Controlador**: Lógica de controladores en `org.nexus.indexador.controllers`

Además, contiene utilidades para mejorar el rendimiento:
- Sistema de logging centralizado
- Sistema de caché de imágenes con referencias suaves para gestión de memoria

## Contribución

Si quieres contribuir al proyecto, sigue estos pasos:

1. Haz un fork del proyecto
2. Crea una nueva rama (`git checkout -b feature/nueva-caracteristica`)
3. Realiza los cambios necesarios y commitea (`git commit -am 'Añade nueva característica'`)
4. Haz push a la rama (`git push origin feature/nueva-caracteristica`)
5. Envía una solicitud de extracción (Pull Request)

## Reportar Problemas

Si encuentras algún problema o tienes sugerencias, por favor [crea un issue](https://github.com/Lorwik/Indexador-Nexus/issues/new) con los siguientes detalles:

- Descripción del problema
- Pasos para reproducirlo
- Comportamiento esperado
- Capturas de pantalla (si aplica)
- Versión de Java y del sistema operativo

## Licencia

Este proyecto está licenciado bajo GPL-3.0 license.
