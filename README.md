# Hospital Vida Salud

## Sistema de gestión hospitalaria con microservicios

### Descripción del proyecto

Hospital Vida Salud es un proyecto académico desarrollado para gestionar pacientes y citas médicas mediante una arquitectura de microservicios. Los componentes se ejecutan de forma independiente y se comunican entre sí para proporcionar los servicios del sistema.

### Arquitectura del sistema

El proyecto está compuesto por los siguientes microservicios:

- **Eureka Server:** permite el registro y descubrimiento de los servicios.
- **Pacientes Service:** gestiona la información de los pacientes.
- **Citas Service:** gestiona las citas médicas y la comunicación relacionada con los eventos de citas.
- **API Gateway:** centraliza el acceso a los servicios mediante rutas HTTP.

### Tecnologías utilizadas

- Java 17
- Spring Boot
- Spring Cloud Netflix Eureka
- Spring Cloud Gateway
- Maven
- PostgreSQL
- R2DBC
- RabbitMQ
- Git y GitHub
- Postman para pruebas de API

### Puertos de los servicios

| Componente | Puerto |
|---|---:|
| Eureka Server | 8761 |
| Pacientes Service | 8081 |
| Citas Service | 8082 |
| API Gateway | 8083 |

### Rutas de acceso

El API Gateway permite acceder a los servicios mediante las siguientes rutas:

- Pacientes: `/api/pacientes/**`
- Citas: `/api/citas/**`

### Bases de datos

El sistema utiliza PostgreSQL para almacenar información:

- `hospital_vida_salud`: base de datos del servicio de pacientes.
- `hospital_citas`: base de datos del servicio de citas.

### Mensajería

RabbitMQ se utiliza para la comunicación mediante mensajes relacionados con las citas médicas. La cola configurada para las citas es `cola.citas`.

### Ejecución del proyecto

1. Instalar Java 17, Maven, PostgreSQL y RabbitMQ.
2. Configurar las bases de datos y las variables de entorno necesarias.
3. Iniciar Eureka Server.
4. Iniciar Pacientes Service.
5. Iniciar Citas Service.
6. Iniciar API Gateway.
7. Verificar el registro de los servicios en Eureka y probar las rutas HTTP con un navegador o Postman.

### Objetivo académico

Aplicar los conceptos de arquitectura de microservicios, desarrollo de API REST, persistencia de datos, descubrimiento de servicios, enrutamiento y mensajería asíncrona.

**Proyecto académico — Cibertec**
