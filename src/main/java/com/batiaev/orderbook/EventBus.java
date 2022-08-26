package com.batiaev.orderbook;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.lmax.disruptor.RingBuffer;

public interface EventBus {
    RingBuffer<OrderBookUpdateEvent> start();

    void clear();
}
