package com.batiaev.orderbook.serializer;

import com.batiaev.orderbook.events.ModelEvent;
import com.batiaev.orderbook.events.OrderBookSnapshotEvent;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public class CoinbaseModule extends SimpleModule {
    public static final String GROUP_ID = "com.batiaev.md";
    public static final String ARTIFACT_ID = "coinbase-client";
    public static final Version VERSION = new Version(1, 0, 0, "SNAPSHOT",
            GROUP_ID, ARTIFACT_ID);

    public CoinbaseModule() {
        super(CoinbaseModule.class.getSimpleName(), VERSION, Map.of(
                Instant.class, InstantDeserializer.INSTANT,
                LocalDate.class, LocalDateDeserializer.INSTANCE));
        addDeserializer(OrderBookUpdateEvent.class, OrderBookUpdateEventDeserializer.INSTANCE);
        addDeserializer(OrderBookSnapshotEvent.class, OrderBookSnapshotEventDeserializer.INSTANCE);
        addDeserializer(ModelEvent.class, ModelEventDeserializer.INSTANCE);
    }
}
