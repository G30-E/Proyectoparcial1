package com.demo.consumer.service;

import com.demo.consumer.config.RabbitMQConfig;
import com.demo.consumer.model.Transaccion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class RabbitConsumerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiPostService apiPostService = new ApiPostService();

    public void escucharColas(List<String> colas) {
        try {
            Connection connection = RabbitMQConfig.getFactory().newConnection();
            Channel channel = connection.createChannel();

            channel.basicQos(1);

            for (String cola : colas) {
                channel.queueDeclare(cola, true, false, false, null);

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String mensaje = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    long deliveryTag = delivery.getEnvelope().getDeliveryTag();

                    try {
                        System.out.println("Mensaje recibido de cola [" + cola + "]");

                        Transaccion transaccion = objectMapper.readValue(mensaje, Transaccion.class);

                   
                        if (transaccion.getDetalle() != null) {
                            String descripcionActual = transaccion.getDetalle().getDescripcion();

                            String nuevaDescripcion =
                                    (descripcionActual != null ? descripcionActual : "") +
                                    " - Estudiante: Geofrey Florian" +
                                    " - Carnet: 0905-24-17570" +
                                    " - UUID: d67afaff-e7d2-4ba0-acf1-ea070a249ea5";

                            transaccion.getDetalle().setDescripcion(nuevaDescripcion);
                            
                            System.out.println("Descripcion final enviada al POST: " + transaccion.getDetalle().getDescripcion());
                        }

                        boolean exito = apiPostService.enviarTransaccion(transaccion);

                        if (exito) {
                            channel.basicAck(deliveryTag, false);
                            System.out.println("ACK enviado para: " + transaccion.getIdTransaccion());
                        } else {
                            System.err.println("POST falló, se reencola: " + transaccion.getIdTransaccion());
                            channel.basicNack(deliveryTag, false, true);
                        }

                    } catch (Exception e) {
                        System.err.println("Error procesando mensaje: " + e.getMessage());
                        channel.basicNack(deliveryTag, false, true);
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
}