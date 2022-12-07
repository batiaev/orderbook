package com.batiaev.orderbook.serializer;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.InstantKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.LocalDateKeyDeserializer;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public class OrderBookModel extends SimpleModule {

    public static final String GROUP_ID = "com.batiaev";
    public static final String ARTIFACT_ID = "orderbook";
    public static final Version VERSION = new Version(1, 0, 4, "",
            GROUP_ID, ARTIFACT_ID);

    public OrderBookModel() {
        super(OrderBookModel.class.getSimpleName(), VERSION,
                Map.of(Instant.class, InstantDeserializer.INSTANT,
                        LocalDate.class, LocalDateDeserializer.INSTANCE));

        addSerializer(PriceLevelSerialiser.INSTANCE);
        addKeyDeserializer(LocalDate.class, LocalDateKeyDeserializer.INSTANCE);
        addKeyDeserializer(Instant.class, InstantKeyDeserializer.INSTANCE);
    }
}
