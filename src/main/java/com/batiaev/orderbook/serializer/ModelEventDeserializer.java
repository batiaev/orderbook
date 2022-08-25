package com.batiaev.orderbook.serializer;

import com.batiaev.orderbook.events.Event;
import com.batiaev.orderbook.events.ModelEvent;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Set;

import static com.batiaev.orderbook.events.OrderBookSubscribeEvent.subscribeOn;

public class ModelEventDeserializer extends StdDeserializer<ModelEvent> {
    public final static ModelEventDeserializer INSTANCE = new ModelEventDeserializer();
    public static final Set<String> REQUIRED_FIELDS = Set.of("type");

    public ModelEventDeserializer() {
        super(ModelEvent.class);
    }

    @Override
    public ModelEvent deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        final JsonNode node = jp.getCodec().readTree(jp);
        var type = Event.Type.valueOf(node.get("type").asText().toUpperCase());
        var orderBookEvent = switch (type) {
            case SUBSCRIBE -> subscribeOn(node.get("product_ids").get(0).asText(), node.get("channel").asText());
            case SNAPSHOT -> OrderBookSnapshotEventDeserializer.parse(jp, node);
            case L2UPDATE -> OrderBookUpdateEventDeserializer.parse(jp, node);
            case UNKNOWN -> null;
        };
        return new ModelEvent(type, orderBookEvent);
    }
}
