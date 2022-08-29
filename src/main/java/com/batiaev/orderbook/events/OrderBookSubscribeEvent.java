package com.batiaev.orderbook.events;

import com.batiaev.orderbook.model.ProductId;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public record OrderBookSubscribeEvent(String channel, List<ProductId> productId) {
    public static OrderBookSubscribeEvent withEvent(String channel, String... productId) {
        return new OrderBookSubscribeEvent(channel, Arrays.stream(productId).map(ProductId::productId).collect(toList()));
    }

    public static OrderBookSubscribeEvent withEvent(String channel, ProductId... productId) {
        return new OrderBookSubscribeEvent(channel, List.of(productId));
    }

    public String toJson() {
        String collect = productId.stream().map(p -> "\"" + p.id() + "\"").collect(joining(", "));
        return "{\"type\": \"subscribe\", \"product_ids\": [" + collect + "], \"channels\": [\"" + channel + "\"]}";
    }

    @Override
    public String toString() {
        return "OrderBookSubscribeEvent" +
                toJson();
    }
}
