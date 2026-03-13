package com.demo.consumer.service;

import com.demo.consumer.model.Transaccion;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiPostService {

    private static final String POST_URL =
            "https://7e0d9ogwzd.execute-api.us-east-1.amazonaws.com/default/guardarTransacciones";


    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiPostService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public boolean enviarTransaccion(Transaccion transaccion) {
        try {
            String json = objectMapper.writeValueAsString(transaccion);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(POST_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Respuesta POST: " + response.statusCode() + " - " + response.body());

            return response.statusCode() == 200 || response.statusCode() == 201;

        } catch (Exception e) {
            System.err.println("Error enviando al POST: " + e.getMessage());
            return false;
        }
    }
}