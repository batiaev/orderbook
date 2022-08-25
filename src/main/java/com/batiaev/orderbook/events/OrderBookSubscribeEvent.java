package com.batiaev.orderbook.events;

import com.batiaev.orderbook.model.ProductId;

public record OrderBookSubscribeEvent(ProductId productId, String channel) implements OrderBookEvent {
    public static OrderBookSubscribeEvent subscribeOn(String productId, String channel) {
        return subscribeOn(ProductId.productId(productId), channel);
    }

    public static OrderBookSubscribeEvent subscribeOn(ProductId productId, String channel) {
        return new OrderBookSubscribeEvent(productId, channel);
    }

    @Override
    public Type type() {
        return Type.SUBSCRIBE;
    }

    public String toJson() {
        return "{\"type\": \"subscribe\", \"product_ids\": [\"" + productId.id() + "\"], \"channels\": [\"" + channel + "\"]}";
    }

    @Override
    public String toString() {
        return "OrderBookSubscribeEvent" +
                toJson();
    }
}
