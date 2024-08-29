package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    static class Ticket {
        String origin;
        String destination;
        String carrier;
        Long flightTimeMinutes;
        Integer price;

        Ticket(String origin, String destination, String carrier, long flightTimeMinutes, int price) {
            this.origin = origin;
            this.destination = destination;
            this.carrier = carrier;
            this.flightTimeMinutes = flightTimeMinutes;
            this.price = price;
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        List<Ticket> tickets = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        InputStream resourceStream = Main.class.getClassLoader().getResourceAsStream("tickets.json");
        if (resourceStream == null) {
            throw new IllegalArgumentException("File not found!");
        }

        JsonNode root = mapper.readTree(resourceStream);
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yy HH:mm");

        for (JsonNode ticketNode : root.path("tickets")) {
            String origin = ticketNode.path("origin").asText();
            String destination = ticketNode.path("destination").asText();

            if (origin.equals("VVO") && destination.equals("TLV")) {
                String carrier = ticketNode.path("carrier").asText();
                int price = ticketNode.path("price").asInt();

                String departureTimeStr = ticketNode.path("departure_date").asText() + " " + ticketNode.path("departure_time").asText();
                String arrivalTimeStr = ticketNode.path("arrival_date").asText() + " " + ticketNode.path("arrival_time").asText();

                Date departureTime = dateTimeFormat.parse(departureTimeStr);
                Date arrivalTime = dateTimeFormat.parse(arrivalTimeStr);

                long flightTimeMinutes = (arrivalTime.getTime() - departureTime.getTime()) / (60 * 1000);

                tickets.add(new Ticket(origin, destination, carrier, flightTimeMinutes, price));
            }
        }

        calculateAndPrintResults(tickets);
    }

    private static void calculateAndPrintResults(List<Ticket> tickets) {
        long minFlightTime = tickets.stream()
                .mapToLong(t -> t.flightTimeMinutes)
                .min()
                .orElse(0);

        double avgPrice = tickets.stream()
                .mapToInt(t -> t.price)
                .average()
                .orElse(0);

        List<Integer> prices = tickets.stream()
                .map(t -> t.price)
                .sorted()
                .toList();

        double medianPrice;
        int size = prices.size();
        if (size % 2 == 0) {
            medianPrice = (prices.get(size / 2 - 1) + prices.get(size / 2)) / 2.0;
        } else {
            medianPrice = prices.get(size / 2);
        }

        double priceDifference = avgPrice - medianPrice;

        System.out.printf("Маршрут: Владивосток - Тель-Авив%n");
        System.out.printf("Минимальное время полета: %d минут%n", minFlightTime);
        System.out.printf("Средняя цена: %.2f%n", avgPrice);
        System.out.printf("Медианная цена: %.2f%n", medianPrice);
        System.out.printf("Разница в цене (средняя - медиана): %.2f%n", priceDifference);
    }
}