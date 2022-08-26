package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.lmax.disruptor.EventHandler;

public interface OrderBookEventHandler extends EventHandler<OrderBookUpdateEvent> {
    default void clear() {
    }
}
