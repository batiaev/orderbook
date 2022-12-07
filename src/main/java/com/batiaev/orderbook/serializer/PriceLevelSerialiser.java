package com.batiaev.orderbook.serializer;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class PriceLevelSerialiser extends StdSerializer<OrderBookUpdateEvent.PriceLevel> {

    public static final PriceLevelSerialiser INSTANCE = new PriceLevelSerialiser();

    public PriceLevelSerialiser() {
        super(OrderBookUpdateEvent.PriceLevel.class);
    }

    @Override
    public void serialize(OrderBookUpdateEvent.PriceLevel value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        gen.writeStartObject();
        gen.writeNumberField("size", value.size());
        gen.writeNumberField("priceLevel", value.priceLevel());
        gen.writeStringField("side", value.side().name());
        gen.writeEndObject();
    }
}
