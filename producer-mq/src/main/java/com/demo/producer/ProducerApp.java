package com.demo.producer;

import com.demo.producer.model.LoteTransacciones;
import com.demo.producer.service.ApiService;
import com.demo.producer.service.RabbitProducerService;

public class ProducerApp {

    public static void main(String[] args) {
        ApiService apiService = new ApiService();
        RabbitProducerService producerService = new RabbitProducerService();

        try {
            LoteTransacciones lote = apiService.obtenerLote();

            System.out.println("Lote recibido: " + lote.getLoteId());
            System.out.println("Cantidad de transacciones: " + lote.getTransacciones().size());

            producerService.enviarTodas(lote.getTransacciones());

            System.out.println("Proceso del Producer finalizado.");

        } catch (Exception e) {
            System.err.println("Error en ProducerApp: " + e.getMessage());
            e.printStackTrace();
        }
    }
}