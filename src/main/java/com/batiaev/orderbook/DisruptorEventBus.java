package com.batiaev.orderbook;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.handlers.OrderBookEventHandler;
import com.batiaev.orderbook.model.ProductId;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.neovisionaries.ws.client.WebSocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisruptorEventBus implements EventBus {
    public static final EventFactory<OrderBookUpdateEvent> EVENT_FACTORY = OrderBookUpdateEvent::new;
    private static final Map<WebSocket, List<ProductId>> connections = new HashMap<>();
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private final Disruptor<OrderBookUpdateEvent> disruptor;
    private volatile RingBuffer<OrderBookUpdateEvent> ringBuffer;
    private final OrderBookEventHandler[] eventHandler;

    public DisruptorEventBus(OrderBookEventHandler... eventHandler) {
        this.eventHandler = eventHandler;
        this.disruptor = new Disruptor<>(EVENT_FACTORY,
                DEFAULT_BUFFER_SIZE,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new BusySpinWaitStrategy());
        if (eventHandler.length == 0)
            throw new IllegalArgumentException("should be at least one handler");
        final var handlerGroup = disruptor.handleEventsWith(eventHandler[0]);
        for (int i = 1; i < eventHandler.length; i++) {
            handlerGroup.then(eventHandler[i]);
        }
    }

    @Override
    public RingBuffer<OrderBookUpdateEvent> start() {
        if (ringBuffer == null) {
            synchronized (this) {
                if (ringBuffer == null) {
                    ringBuffer = disruptor.start();
                }
            }
        }
        return ringBuffer;
    }

    @Override
    public void clear() {
        for (OrderBookEventHandler handler : eventHandler) {
            handler.clear();
        }
    }
}
