# RutaExpress — Entrega Parcial 2 (EP2)

Sistema académico de logística y seguimiento de envíos, desarrollado con arquitectura de microservicios, mensajería asíncrona, seguridad JWT y despliegue en AWS.

## 1. Arquitectura

El frontend Angular consume las API a través de AWS API Gateway y del Backend for Frontend (BFF).

El backend utiliza microservicios Spring Boot, Oracle Database, RabbitMQ y componentes de mensajería y auditoría.

**Flujo principal:**

```text
Angular → API Gateway → BFF → Microservicios → RabbitMQ → Consumidores → Oracle
```

**Aplicación web:** https://rutaexpress-duoc.netlify.app

## 2. Componentes

| Componente | Puerto interno | Función |
|---|---:|---|
| Shipments | 8080 | Gestión y seguimiento de envíos |
| BFF | 8081 | Entrada al backend y seguridad JWT |
| Notify | 8082 | Procesamiento de EMAIL y LABEL |
| Catalog | 8083 | Catálogo, capacidad y tickets de bodega |
| Audit | 8084 | Auditoría de eventos |
| Report | 8085 | Reportes |
| Rabbit Admin | 8086 | Administración de RabbitMQ mediante REST |
| Oracle | 1521 | Persistencia de datos |
| RabbitMQ | 5672 | Mensajería AMQP |
| RabbitMQ Management | 15672 | Administración y métricas |

## 3. Tecnologías

- Java 21 y Spring Boot
- Spring Security y OAuth2 Resource Server
- Microsoft Entra ID y JWT
- Angular y TypeScript
- Oracle Database
- RabbitMQ
- Apache Kafka y Zookeeper
- Docker y Docker Compose
- Maven
- AWS EC2, API Gateway y balanceador de carga
- Netlify para el frontend

## 4. Estructura de repositorios

Para la EP2, los componentes principales se separaron en repositorios independientes.

### Repositorio principal `rutaexpress`

Este repositorio conserva los componentes auxiliares y la infraestructura compartida:

```text
rutaexpress/
├── infra/
├── ms-rutaexpress-audit/
├── ms-rutaexpress-notify/
└── ms-rutaexpress-report/
```

### Repositorios independientes

- `rutaexpress-frontend` — Frontend Angular + MSAL.
- `rutaexpress-shipments` — Microservicio principal de envíos.
- `rutaexpress-bff` — Backend for Frontend.
- `rutaexpress-catalog` — Microservicio de catálogo.
- `rutaexpress-rabbit-admin` — Microservicio administrador de RabbitMQ.

Esta separación permite entregar de forma independiente los componentes principales solicitados para la evaluación, manteniendo en el repositorio general los servicios auxiliares y la infraestructura compartida.

## 5. RabbitMQ — EP2

RutaExpress utiliza mensajería asíncrona para desacoplar la publicación de comandos y su procesamiento.

### Colas principales

- `q.cmd.email`
- `q.cmd.label`
- `q.cmd.warehouse`

### Colas de errores (DLQ)

- `q.cmd.email.dlq`
- `q.cmd.label.dlq`
- `q.cmd.warehouse.dlq`

Los consumidores procesan los mensajes y confirman su recepción mediante ACK.

Ante errores, el sistema permite probar reintentos limitados y el envío de mensajes fallidos a las DLQ mediante NACK y configuración de dead lettering.

### Exchanges

La topología utiliza exchanges de tipo `direct` y `topic`, además de un exchange de dead letter para enrutar mensajes fallidos hacia sus respectivas DLQ.

### Política de colas

La política `rutaexpress-ttl-max` configura:

- TTL de mensajes: 86.400.000 ms (24 horas).
- Capacidad máxima: 1.000 mensajes por cola principal.
- Desbordamiento: comportamiento `drop-head` por defecto.

Las colas principales tienen consumidores dedicados. En las pruebas se verificó ACK y un prefetch de 5 para el consumidor EMAIL.

### Administración REST

Rabbit Admin permite realizar operaciones de administración sobre colas, exchanges y bindings mediante rutas protegidas.

Ejemplos:

- `POST /api/admin/colas`
- `GET /api/admin/colas/{nombre}`
- `DELETE /api/admin/colas/{nombre}`
- `POST /api/admin/exchanges`
- `DELETE /api/admin/exchanges/{nombre}`
- `POST /api/admin/bindings`
- `DELETE /api/admin/bindings`

Las operaciones administrativas requieren un usuario con rol `Admin`.

## 6. Persistencia en Oracle

Los microservicios utilizan esquemas separados según su responsabilidad.

| Esquema | Responsabilidad |
|---|---|
| `RUTAEXPRESS_SHIPMENTS` | Envíos |
| `RUTAEXPRESS_CATALOG` | Catálogo y tickets de bodega |
| `RUTAEXPRESS_AUDIT` | Auditoría |
| `RUTAEXPRESS_REPORT` | Reportes |

Ejemplo de persistencia comprobada: un envío aceptado puede generar un ticket en `RUTAEXPRESS_CATALOG.TICKETS_BODEGA` mediante el consumidor WAREHOUSE.

## 7. Seguridad

La autenticación utiliza tokens JWT de Microsoft Entra ID.

El BFF protege los endpoints según los permisos del usuario.

Para las rutas administrativas se comprobaron estos escenarios:

| Escenario | Respuesta HTTP |
|---|---|
| Solicitud sin token | 401 Unauthorized |
| Solicitud autorizada de Admin | 201 Created en creación de cola |
| Usuario Cliente sin permiso de Admin | 403 Forbidden |
| Petición administrativa inválida | 400 Bad Request |

Los secretos, contraseñas, tokens y claves privadas no deben incluirse en los repositorios.

## 8. Compilación

### Frontend Angular

El frontend se encuentra en el repositorio independiente `rutaexpress-frontend`.

Desde la raíz de ese repositorio:

```bash
npm ci
npm run build
```

Los archivos de producción se generan en `dist/`.

### Microservicios Java

Cada microservicio dispone de Maven Wrapper.

En Windows, desde la carpeta raíz de cada microservicio:

```powershell
.\mvnw.cmd -DskipTests clean package
```

Los archivos JAR se generan en `target/`.

El argumento `-DskipTests` omite la ejecución de las pruebas automatizadas; no equivale a ejecutar la batería completa de tests.

## 9. Despliegue en AWS

La infraestructura se distribuye en instancias EC2 según la responsabilidad del componente.

### `ec2-apps`

Contiene los microservicios de la aplicación y Oracle.

Archivo de despliegue:

```text
infra/apps/compose.yml
```

Desde la carpeta correspondiente en EC2:

```bash
sudo docker compose up -d
sudo docker compose ps
```

### `ec2-mq`

Contiene el clúster RabbitMQ de dos nodos.

Archivo de despliegue:

```text
infra/mq/compose.yml
```

Para comprobar el clúster:

```bash
sudo docker exec rabbitmq1 rabbitmqctl cluster_status
```

**Nota:** los comandos de despliegue requieren que las imágenes y las variables de entorno necesarias estén configuradas previamente.

Las IP públicas de EC2 pueden cambiar al reiniciar el AWS Learner Lab.

RabbitMQ Management debe accederse de manera controlada, por ejemplo, mediante un túnel SSH. No se recomienda publicar su puerto de administración abiertamente en Internet.

## 10. Pruebas funcionales realizadas

Durante la EP2 se comprobaron:

- Creación y actualización de envíos desde Angular.
- Acceso al backend mediante API Gateway y BFF.
- Publicación de mensajes EMAIL, LABEL y WAREHOUSE.
- Consumo de mensajes y confirmaciones ACK.
- Reintentos limitados y envío de errores a DLQ.
- Registro en logs de mensajes recibidos por listeners de DLQ.
- Métricas Ready, Unacked y Consumers en RabbitMQ Management.
- Creación y eliminación de colas, exchanges y bindings.
- Seguridad HTTP 401, 403 y acceso autorizado de Admin.
- Persistencia de tickets de bodega en Oracle.
- Compilación del frontend y de los siete proyectos Java.

**Nota:** las notificaciones de correo se generan como archivos `.eml`. Las pruebas no acreditan envío real mediante un servidor SMTP.

## 11. Buenas prácticas

- No subir archivos `.env` con credenciales reales.
- No subir claves `.pem`, archivos privados ni tokens.
- Utilizar variables de entorno para información sensible.
- Mantener excluidas las carpetas `node_modules/`, `target/`, `dist/`, `bin/` y otros archivos generados.
- No utilizar `docker compose down -v` cuando se necesite conservar datos persistentes.
- Verificar el estado de Git antes de hacer commit y push.
- Si una credencial quedó expuesta en el historial de Git, cambiarla de forma segura.

## 12. Contexto académico

Proyecto desarrollado para la evaluación de arquitectura Cloud Native y microservicios, correspondiente a la Entrega Parcial 2 de RutaExpress.