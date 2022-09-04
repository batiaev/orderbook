package com.batiaev.orderbook.serializer;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.Side;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.BiConsumer;

import static com.batiaev.orderbook.events.Event.Type.*;
import static com.batiaev.orderbook.model.ProductId.productId;
import static com.batiaev.orderbook.model.TradingVenue.COINBASE;
import static java.time.Instant.EPOCH;
import static java.util.Arrays.asList;

public class OrderBookEventParser implements EventParser, BiConsumer<OrderBookUpdateEvent, JsonNode> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void parse(OrderBookUpdateEvent event, String text) {
        try {
            accept(event, mapper.readTree(text));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void accept(OrderBookUpdateEvent event, JsonNode node) {
        final var type = node.has("type") ? eventType(node.get("type").asText()) : UNKNOWN;
        if (!type.equals(SNAPSHOT) && !type.equals(L2UPDATE)) {
            event.clear();
            return;
        }
        event.setChanges(switch (type) {
            case SNAPSHOT -> parseSnapshotEvent(node);
            case L2UPDATE -> parseUpdateEvent(node, event.changes());
            case UNKNOWN -> List.of();//unreachable
        });
        final var productId = productId(node.get("product_id").asText());
        final var time = node.has("time") ? Instant.parse(node.get("time").asText()) : EPOCH;
        event.setTime(time);
        event.setVenue(COINBASE);
        event.setProductId(productId);
        event.setType(type);
    }

    public List<OrderBookUpdateEvent.PriceLevel> parseSnapshotEvent(JsonNode node) {
        final var asksNode = node.get("asks");
        final var bidsNode = node.get("bids");
        if (asksNode == null || bidsNode == null) {
            //TODO throw exception to fail fast or log errors
            return List.of();
        }
        final var priceLevels = new OrderBookUpdateEvent.PriceLevel[asksNode.size() + bidsNode.size()];
        for (int idx = 0; idx < asksNode.size(); idx++) {
            priceLevels[idx] = getPriceLevel(asksNode.get(idx), Side.SELL);
        }
        for (int i = 0; i < bidsNode.size(); i++) {
            priceLevels[i + asksNode.size()] = getPriceLevel(bidsNode.get(i), Side.BUY);
        }
        return asList(priceLevels);
    }

    private OrderBookUpdateEvent.PriceLevel getPriceLevel(JsonNode node, Side side) {
        return new OrderBookUpdateEvent.PriceLevel(side,
                new BigDecimal(node.get(0).asText()),
                new BigDecimal(node.get(1).asText())
        );
    }

    private List<OrderBookUpdateEvent.PriceLevel> parseUpdateEvent(JsonNode node, List<OrderBookUpdateEvent.PriceLevel> chngs) {
        var changes = node.get("changes");
        if (chngs.size() == changes.size()) {
            int idx = 0;
            for (JsonNode change : changes) {
                var side = Side.of(change.get(0).asText());
                var level = new BigDecimal(change.get(1).asText());
                var size = new BigDecimal(change.get(2).asText());
                chngs.get(idx++).update(side, level, size);
            }
            return chngs;
        }
        var priceLevels = new OrderBookUpdateEvent.PriceLevel[changes.size()];
        for (int i = 0; i < changes.size(); i++) {
            final var change = changes.get(i);
            var side = Side.of(change.get(0).asText());
            var level = new BigDecimal(change.get(1).asText());
            var size = new BigDecimal(change.get(2).asText());
            priceLevels[i] = new OrderBookUpdateEvent.PriceLevel(side, level, size);
        }
        return asList(priceLevels);
    }
}
