package com.batiaev.orderbook.eventbus;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;

import java.lang.ref.Cleaner;

public interface EventBus extends Cleaner.Cleanable {
    EventEnricher<OrderBookUpdateEvent> start();

    void clean();
}
