package com.batiaev.orderbook.serializer;

import com.batiaev.orderbook.events.OrderBookSnapshotEvent;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Set;

import static com.batiaev.orderbook.model.ProductId.productId;
import static java.lang.Double.parseDouble;

public class OrderBookSnapshotEventDeserializer extends StdDeserializer<OrderBookSnapshotEvent> {
    public final static OrderBookSnapshotEventDeserializer INSTANCE = new OrderBookSnapshotEventDeserializer();
    public static final Set<String> REQUIRED_FIELDS = Set.of("type", "asks", "bids");

    public OrderBookSnapshotEventDeserializer() {
        super(OrderBookSnapshotEvent.class);
    }

    public static OrderBookSnapshotEvent parse(JsonParser jp, JsonNode node) {
        for (String field : REQUIRED_FIELDS) {
            if (!node.has(field)) throw new IllegalStateException("Required field " + field + " is missing");
        }

        double[][] asks = parsePriceLevels(node.get("asks"));
        double[][] bids = parsePriceLevels(node.get("bids"));
        var productId = productId(node.get("product_id").asText());
        return new OrderBookSnapshotEvent(productId, asks, bids);
    }

    private static double[][] parsePriceLevels(JsonNode asks) {
        double[][] ask = new double[asks.size()][];
        for (int i = 0; i < asks.size(); i++) {
            ask[i] = new double[]{parseDouble(asks.get(i).get(0).asText()), parseDouble(asks.get(i).get(1).asText())};
        }
        return ask;
    }

    @Override
    public OrderBookSnapshotEvent deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        final JsonNode node = jp.getCodec().readTree(jp);
        return parse(jp, node);
    }
}
