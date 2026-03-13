# 🧪 Proyecto: Procesamiento de Transacciones Bancarias con RabbitMQ y Java

**Link explicación del proyecto:** https://drive.google.com/drive/folders/1X24MgLiRQsK317q7sq86T7e8py_bcz3D?usp=drive_link

## 📖 Descripción General
Este proyecto implementa una arquitectura distribuida basada en el patrón **Producer–Consumer** utilizando **Java 11**, **Maven** y **RabbitMQ**. El sistema consume un lote de transacciones bancarias desde una API externa, distribuye cada transacción a una cola según el banco destino y posteriormente un consumidor procesa dichas colas para enviar cada transacción a una API **POST**, agregando antes del envío final los datos del estudiante requeridos para la entrega académica.

## 🎯 Objetivo del Proyecto
Desarrollar un sistema que permita consumir transacciones desde una API externa (`GET`), distribuir cada transacción a una cola según su `bancoDestino`, escuchar múltiples colas de RabbitMQ, consumir cada mensaje y enviarlo a una API externa (`POST`), implementar **ACK manual**, evitar pérdida de mensajes en caso de error y agregar en el **Consumer** el nombre, carné y UUID del estudiante antes de persistir la transacción.

## 🔗 APIs Utilizadas

### API de obtención de transacciones
**Método:** `GET`  
**URL:** `https://hly784ig9d.execute-api.us-east-1.amazonaws.com/default/transacciones`  
**Uso en el proyecto:** Esta API es consumida por el **Producer** para obtener el lote completo de transacciones bancarias.

### API de almacenamiento de transacciones
**Método:** `POST`  
**URL:** `https://7e0d9ogwzd.execute-api.us-east-1.amazonaws.com/default/guardarTransacciones`  
**Uso en el proyecto:** Esta API es invocada por el **Consumer** para almacenar cada transacción una vez consumida desde RabbitMQ y enriquecida con los datos del estudiante.

## 🧩 Arquitectura del Sistema

```text
API GET /transacciones
        │
        ▼
Producer (Java + Maven)
        │
        ▼
RabbitMQ
(cola por banco)
        │
        ▼
Consumer (Java + Maven)
        │
        ▼
API POST /guardarTransacciones
🏗️ Componentes del Proyecto
Producer

El Producer tiene la responsabilidad de consumir el endpoint GET /transacciones, obtener el lote de transacciones, recorrer cada transacción, identificar el valor de bancoDestino, declarar dinámicamente la cola correspondiente si no existe y publicar cada transacción en formato JSON dentro de RabbitMQ.

Consumer

El Consumer escucha múltiples colas en RabbitMQ, recibe los mensajes en formato JSON, deserializa cada transacción a objeto Java, agrega en detalle.descripcion los datos del estudiante, envía la transacción al endpoint POST /guardarTransacciones, realiza ACK manual cuando la respuesta es exitosa y, si el POST falla, reencola el mensaje para evitar su pérdida.

👨‍🎓 Datos del Estudiante Agregados en el Consumer

Nombre: Geofrey Florian

Carné: 0905-24-17570

UUID: d67afaff-e7d2-4ba0-acf1-ea070a249ea5

📦 Tecnologías Utilizadas

Java 11

Maven

RabbitMQ

Jackson

Java HttpClient

Docker para ejecutar RabbitMQ localmente

📁 Estructura del Proyecto
Producer
producer-mq
└── src/main/java
    └── com.demo.producer
        ├── ProducerApp.java
        ├── config
        │   └── RabbitMQConfig.java
        ├── model
        │   ├── LoteTransacciones.java
        │   ├── Transaccion.java
        │   ├── Detalle.java
        │   └── Referencias.java
        └── service
            ├── ApiService.java
            └── RabbitProducerService.java
Consumer
consumer-mq
└── src/main/java
    └── com.demo.consumer
        ├── ConsumerApp.java
        ├── config
        │   └── RabbitMQConfig.java
        ├── model
        │   ├── Transaccion.java
        │   ├── Detalle.java
        │   └── Referencias.java
        └── service
            ├── ApiPostService.java
            └── RabbitConsumerService.java
⚙️ Configuración de RabbitMQ

RabbitMQ fue ejecutado mediante Docker utilizando el siguiente comando:

docker run -d --hostname rabbitmq --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
Panel de administración

http://localhost:15672

Credenciales por defecto

Usuario: guest

Contraseña: guest

▶️ Ejecución del Proyecto
1. Iniciar RabbitMQ

Si el contenedor ya existe, se inicia con:

docker start rabbitmq
2. Ejecutar el Producer

Se ejecuta la clase principal:

ProducerApp.java

Este proceso:

consume el endpoint GET

obtiene el lote de transacciones

crea colas por banco

publica las transacciones en RabbitMQ

3. Verificar las colas en RabbitMQ

En el panel de RabbitMQ se visualizan colas como:

BAC

BANRURAL

BI

GYT

4. Ejecutar el Consumer

Se ejecuta la clase principal:

ConsumerApp.java

Este proceso:

escucha las colas declaradas

consume los mensajes

agrega nombre, carné y UUID en detalle.descripcion

envía la transacción al endpoint POST

realiza ACK manual si el envío es exitoso

🔄 Flujo del Sistema

El Producer consume la API GET.

Se obtiene un lote de transacciones bancarias.

Cada transacción se publica en RabbitMQ según el banco destino.

El Consumer escucha las colas configuradas.

Cada mensaje es deserializado a objeto Java.

El Consumer agrega el nombre, el carné y el UUID del estudiante en detalle.descripcion.

La transacción se envía al endpoint POST.

Si la respuesta del POST es exitosa (200 o 201), se realiza ACK manual.

Si el POST falla, se realiza NACK con reencolado para evitar pérdida del mensaje.

✅ Pruebas Realizadas
Prueba 1 – Flujo Correcto Completo

Se comprobó que:

el Producer consume correctamente el endpoint GET

las colas por banco se crean correctamente

RabbitMQ recibe y distribuye los mensajes por banco

el Consumer consume las colas correctamente

el endpoint POST responde exitosamente

se realiza ACK manual

los mensajes bajan a 0 en RabbitMQ una vez procesados

Prueba 2 – POST Fallido

Se modificó temporalmente la URL del endpoint POST para forzar un error. Como resultado:

el POST respondió con error

el mensaje no se perdió

el Consumer no realizó ACK

el mensaje fue reencolado

Prueba 3 – RabbitMQ Apagado

Se realizó una prueba con RabbitMQ apagado para validar el comportamiento del sistema ante falla de conexión. El sistema mostró un error controlado, evidenciando el manejo de errores sin romper el flujo general de la aplicación.

🧠 Manejo de Errores

El sistema contempla y maneja los siguientes escenarios:

error al consumir la API GET

error de conexión con RabbitMQ

error al consumir mensajes

error al enviar al endpoint POST

reencolado automático de mensajes cuando el POST falla

confirmación manual de mensajes únicamente cuando la transacción ha sido procesada exitosamente

📌 Observaciones Técnicas

El Producer únicamente enruta y publica las transacciones en RabbitMQ.

El Consumer es el responsable de enriquecer la transacción con los datos del estudiante antes de enviarla al endpoint final.

La arquitectura se mantiene desacoplada gracias al uso de RabbitMQ como intermediario entre ambos componentes.

El uso de ACK manual garantiza que las transacciones no se eliminen de la cola hasta haber sido procesadas correctamente.

✅ Conclusión

El proyecto cumple con los requisitos planteados para la implementación del patrón Producer–Consumer utilizando RabbitMQ y Java. Se logró una arquitectura desacoplada, distribución dinámica por banco, consumo de múltiples colas, envío controlado a una API externa, manejo de errores, reencolado en caso de fallo y confirmación manual de mensajes para evitar pérdida de información. Además, el sistema incorpora los datos del estudiante en el Consumer antes del almacenamiento final, cumpliendo con lo solicitado para la entrega académica.
