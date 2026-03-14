package com.demo.producer.service;

import com.demo.producer.config.RabbitMQConfig;
import com.demo.producer.model.Transaccion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import java.util.List;

public class RabbitProducerService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void enviarTodas(List<Transaccion> transacciones) {
        try (Connection connection = RabbitMQConfig.getFactory().newConnection();
             Channel channel = connection.createChannel()) {

            for (Transaccion transaccion : transacciones) {
                String nombreCola = transaccion.getBancoDestino();

                channel.queueDeclare(nombreCola, true, false, false, null);

                String mensajeJson = objectMapper.writeValueAsString(transaccion);

                channel.basicPublish(
                        "",
                        nombreCola,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        mensajeJson.getBytes()
                );

                System.out.println("Transacción enviada por Geofrey Florian a [" + nombreCola + "]");
            }

        } catch (Exception e) {
            System.err.println("Error enviando lote a RabbitMQ: " + e.getMessage());
            e.printStackTrace();
        }
    }
}