package com.batiaev.orderbook.eventbus;

import java.util.function.Consumer;

public interface EventEnricher<T> {

    void nextEvent(Consumer<T> enricher);
}
