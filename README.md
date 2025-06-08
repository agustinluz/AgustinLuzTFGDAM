Planora es un pequeño proyecto de planificación de eventos y seguimiento de gastos, desarrollado como trabajo de último año. Se divide en un backend con Spring Boot y un frontend con Ionic Vue.

## Estructura del repositorio

- `BACKAPI/API_APPAMIGOS` – API principal de Spring Boot
 
- `FRONT/planora-fronted` – Interfaz de Ionic + Vue
 
- `planify_esquema.sql` – Esquema MySQL para la aplicación
 
- Las demás carpetas del backend se mantienen como experimentos más antiguos.

## Empezando

El backend y el frontend son proyectos independientes. Cada uno incluye un archivo README con instrucciones detalladas, pero el flujo de trabajo habitual es el siguiente:

``golpe
# iniciar la API
cd BACKAPI/API_APPAMIGOS
./mvnw spring-boot:ejecutar
```

``golpe
# ejecutar el frontend
cd FRONT/planora-frontal
instalación de npm
npm ejecutar dev
```

Asegúrese de que MySQL esté en ejecución y actualice los archivos `application.properties` o `.env` según sea necesario. Para más detalles, consulte:

- [ BACKAPI/API_APPAMIGOS/README.md ]( BACKAPI/API_APPAMIGOS/README.md )
- [ FRONT/planora-fronted/README.md ]( FRONT/planora-fronted/README.md )
