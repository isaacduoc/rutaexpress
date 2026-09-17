RutaExpress Backend

Backend del proyecto RutaExpress, desarrollado con arquitectura de microservicios para la gestión de envíos, catálogo de servicios, auditoría, reportes y notificaciones.

Este repositorio contiene los distintos microservicios del backend, además de la infraestructura necesaria para ejecutar Oracle, Kafka, Zookeeper y RabbitMQ mediante Docker Compose.

Arquitectura

Servicio

Puerto

Base de datos / función

Shipments

8080

Oracle - gestión de envíos

BFF

8081

Backend for Frontend / seguridad JWT

Notify

8082

Notificaciones

Catalog

8083

Oracle - catálogo y capacidad

Audit

8084

Oracle - auditoría de eventos

Report

8085

Oracle - reportes

Oracle

1521

Base de datos

RabbitMQ

5672

Mensajería

RabbitMQ Management

15672

Panel web

Kafka

9092

Eventos

Zookeeper

2181

Coordinación de Kafka

Tecnologías utilizadas

Java 21

Spring Boot

Spring Web

Spring Data JPA

Spring Security

OAuth2 Resource Server / JWT

Microsoft Entra ID / MSAL

Oracle Database

Apache Kafka

Zookeeper

RabbitMQ

Docker / Docker Compose

Maven

Estructura del repositorio

backend-rutaexpress-main/
│
├── README.md
├── backend/                     # ms-rutaexpress-shipments
├── ms-rutaexpress-bff/
├── ms-rutaexpress-catalog/
├── ms-rutaexpress-notify/
├── ms-rutaexpress-audit/
└── ms-rutaexpress-report/

Bases de datos

Los microservicios persistentes utilizan Oracle con esquemas separados:

Microservicio

Esquema Oracle

Shipments

RUTAEXPRESS_SHIPMENTS

Catalog

RUTAEXPRESS_CATALOG

Audit

RUTAEXPRESS_AUDIT

Report

RUTAEXPRESS_REPORT

Tablas principales:

ENVIOS

SERVICIOS_CAT

AUDIT_EVENTS

El microservicio de notificaciones no utiliza base de datos.

Flujo principal

Creación de envío

El cliente crea un envío.

Shipments guarda el envío en Oracle.

Se genera un código de seguimiento.

Se publica un evento en Kafka.

Audit registra el evento.

Report procesa los eventos necesarios.

Cambio de estado

Estados utilizados:

CREADO
ACEPTADO
EN_BODEGA
EN_RUTA
ENTREGADO
CANCELADO

Cuando un envío pasa a ACEPTADO, se descuenta una unidad de capacidad del servicio seleccionado.

Si un envío que ya había consumido capacidad pasa a CANCELADO, la capacidad se devuelve al servicio correspondiente.

Catálogo

Catalog administra:

nombre del servicio

tarifa base

capacidad disponible

Endpoints principales:

GET  /api/catalog/services
POST /api/catalog/services
PUT  /api/catalog/services/{id}/descontar
PUT  /api/catalog/services/{id}/devolver

Auditoría

Audit consume eventos desde Kafka y almacena el historial de cambios de cada envío en Oracle.

Endpoints principales:

GET /api/audit
GET /api/audit/shipment/{shipmentId}
GET /api/audit/tracking/{codigoSeguimiento}
GET /api/audit/entregados-hoy

BFF

El BFF funciona como punto de entrada para el frontend.

Rutas principales:

/bff/v1/catalog
/bff/v1/audit
/bff/v1/report

Las solicitudes protegidas utilizan JWT emitidos por Microsoft Entra ID.

Infraestructura con Docker

El archivo docker-compose.yml se encuentra dentro de:

ms-rutaexpress-bff/

Servicios de infraestructura:

Oracle Free

RabbitMQ

Zookeeper

Kafka

Levantar infraestructura

cd C:\backend-rutaexpress-main\ms-rutaexpress-bff
docker compose up -d oracle rabbitmq zookeeper kafka
docker ps

Orden recomendado para ejecutar el backend

1. Shipments

cd C:\backend-rutaexpress-main\backend
.\mvnw.cmd spring-boot:run

2. Catalog

cd C:\backend-rutaexpress-main\ms-rutaexpress-catalog
.\mvnw.cmd spring-boot:run

3. Report

cd C:\backend-rutaexpress-main\ms-rutaexpress-report
.\mvnw.cmd spring-boot:run

4. Audit

cd C:\backend-rutaexpress-main\ms-rutaexpress-audit
.\mvnw.cmd spring-boot:run

5. Notify

cd C:\backend-rutaexpress-main\ms-rutaexpress-notify
.\mvnw.cmd spring-boot:run

6. BFF

cd C:\backend-rutaexpress-main\ms-rutaexpress-bff
.\mvnw.cmd spring-boot:run

Oracle

Service name:

FREEPDB1

JDBC:

jdbc:oracle:thin:@//localhost:1521/FREEPDB1

Las credenciales no deben publicarse en GitHub. Se recomienda usar variables de entorno o archivos locales excluidos por .gitignore.

Kafka

Topic principal:

shipments.events

Los eventos contienen:

ID del envío

código de seguimiento

estado

fecha

correo del destinatario

servicio

RabbitMQ

Configuración local:

Host: localhost
Port: 5672

Panel:

http://localhost:15672

Seguridad

El backend utiliza Spring Security como Resource Server.

La autenticación se realiza mediante Microsoft Entra ID y JWT.

Detener el proyecto

Detener cada microservicio con:

Ctrl + C

Luego:

docker compose down

Evitar:

docker compose down -v

si se desea conservar la información almacenada en los volúmenes de Oracle.

Antes de subir a GitHub

No subir:

contraseñas

tokens

secretos

credenciales de Oracle

carpetas target/

configuración local sensible

archivos temporales del IDE

Revisar los .gitignore antes de ejecutar:

git add .

Proyecto académico

Proyecto desarrollado como parte de una evaluación académica para la implementación de una arquitectura de microservicios aplicada a un sistema de logística y seguimiento de envíos.