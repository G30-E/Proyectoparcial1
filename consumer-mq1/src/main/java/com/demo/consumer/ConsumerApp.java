package com.demo.consumer;

import com.demo.consumer.service.RabbitConsumerService;

import java.util.Arrays;
import java.util.List;

public class ConsumerApp {

    public static void main(String[] args) {
        RabbitConsumerService consumerService = new RabbitConsumerService();

        List<String> colas = Arrays.asList("BAC", "BANRURAL", "BI", "GYT");

        consumerService.escucharColas(colas);

        System.out.println("Consumer iniciado.");
    }
}