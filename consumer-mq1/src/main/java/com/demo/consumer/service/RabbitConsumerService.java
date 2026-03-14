package com.demo.consumer.service;

import com.demo.consumer.config.RabbitMQConfig;
import com.demo.consumer.model.Transaccion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RabbitConsumerService {

    private static final String COLA_DUPLICADOS = "cola_duplicados";
    private static final String COLA_ERRORES = "cola_errores";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiPostService apiPostService = new ApiPostService();

    private final Set<String> idsProcesados = ConcurrentHashMap.newKeySet();

    public void escucharColas(List<String> colas) {
        try {
            Connection connection = RabbitMQConfig.getFactory().newConnection();
            Channel channel = connection.createChannel();

            channel.basicQos(1);

            channel.queueDeclare(COLA_DUPLICADOS, true, false, false, null);
            channel.queueDeclare(COLA_ERRORES, true, false, false, null);

            for (String cola : colas) {
                channel.queueDeclare(cola, true, false, false, null);

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String mensaje = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    long deliveryTag = delivery.getEnvelope().getDeliveryTag();

                    try {
                        Transaccion transaccion = objectMapper.readValue(mensaje, Transaccion.class);
                        String idTransaccion = transaccion.getIdTransaccion();

                        System.out.println("Atendiendo cola: " + cola);
                        System.out.println("ID solicitud procesada: " + idTransaccion);

                        if (idsProcesados.contains(idTransaccion)) {
                            enviarACola(channel, COLA_DUPLICADOS, mensaje);

                            System.out.println("idTransaccion: " + idTransaccion);
                            System.out.println("estado: DUPLICADA");
                            System.out.println("cola destino: " + COLA_DUPLICADOS);
                            System.out.println("-----------------------------------");

                            channel.basicAck(deliveryTag, false);
                            return;
                        }

                        if (transaccion.getDetalle() != null) {
                            String descripcionActual = transaccion.getDetalle().getDescripcion();

                            String nuevaDescripcion =
                                    (descripcionActual != null ? descripcionActual : "") +
                                    " - Estudiante: Geofrey Florian" +
                                    " - Carnet: 0905-24-17570" +
                                    " - UUID: d67afaff-e7d2-4ba0-acf1-ea070a249ea5";

                            transaccion.getDetalle().setDescripcion(nuevaDescripcion);
                        }

                        System.out.println("Descripcion final enviada al POST: " +
                                (transaccion.getDetalle() != null
                                        ? transaccion.getDetalle().getDescripcion()
                                        : "Sin detalle"));

                        boolean exito = apiPostService.enviarTransaccion(transaccion);

                        if (exito) {
                            idsProcesados.add(idTransaccion);

                            System.out.println("idTransaccion: " + idTransaccion);
                            System.out.println("estado: PROCESADA");
                            System.out.println("cola destino: POST");
                            System.out.println("-----------------------------------");

                            channel.basicAck(deliveryTag, false);
                        } else {
                            enviarACola(channel, COLA_ERRORES, mensaje);

                            System.err.println("idTransaccion: " + idTransaccion);
                            System.err.println("estado: ERROR");
                            System.err.println("cola destino: " + COLA_ERRORES);
                            System.err.println("-----------------------------------");

                            channel.basicAck(deliveryTag, false);
                        }

                    } catch (Exception e) {
                        try {
                            enviarACola(channel, COLA_ERRORES, mensaje);
                        } catch (Exception ex) {
                            System.err.println("Error enviando a cola_errores: " + ex.getMessage());
                        }

                        System.err.println("Error procesando mensaje: " + e.getMessage());
                        System.err.println("cola destino: " + COLA_ERRORES);
                        System.err.println("-----------------------------------");

                        channel.basicAck(deliveryTag, false);
                    }
                };

                channel.basicConsume(cola, false, deliverCallback, consumerTag -> {
                });

                System.out.println("Escuchando cola: " + cola);
            }

        } catch (Exception e) {
            System.err.println("Error en consumidor RabbitMQ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void enviarACola(Channel channel, String nombreCola, String mensaje) throws Exception {
        channel.queueDeclare(nombreCola, true, false, false, null);
        channel.basicPublish(
                "",
                nombreCola,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                mensaje.getBytes(StandardCharsets.UTF_8)
        );
    }
}