package com.batiaev.orderbook.serializer;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.events.PriceLevel;
import com.batiaev.orderbook.model.Side;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.batiaev.orderbook.model.ProductId.productId;

public class OrderBookUpdateEventDeserializer extends StdDeserializer<OrderBookUpdateEvent> {
    public final static OrderBookUpdateEventDeserializer INSTANCE = new OrderBookUpdateEventDeserializer();
    public static final Set<String> REQUIRED_FIELDS = Set.of("product_id", "time", "changes");

    public OrderBookUpdateEventDeserializer() {
        super(OrderBookUpdateEvent.class);
    }

    public static OrderBookUpdateEvent parse(JsonParser jp, JsonNode node) {
        for (String field : REQUIRED_FIELDS) {
            if (!node.has(field)) throw new IllegalStateException("Required field " + field + " is missing");
        }

        var productId = productId(node.get("product_id").asText());
        var time = Instant.parse(node.get("time").asText());
        var changes = node.get("changes");
        List<PriceLevel> priceLevels = new ArrayList<>();
        for (int i = 0; i < changes.size(); i++) {
            var side = Side.valueOf(changes.get(i).get(0).asText().toUpperCase());
            var level = new BigDecimal(changes.get(i).get(1).asText());
            var size = new BigDecimal(changes.get(i).get(2).asText());
            priceLevels.add(new PriceLevel(side, level, size));
        }
        return new OrderBookUpdateEvent(productId, time, priceLevels);
    }

    @Override
    public OrderBookUpdateEvent deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        final JsonNode node = jp.getCodec().readTree(jp);
        return parse(jp, node);
    }
}
