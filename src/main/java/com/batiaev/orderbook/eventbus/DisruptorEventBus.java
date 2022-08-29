package com.batiaev.orderbook.eventbus;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.handlers.DisruptorEventEnricher;
import com.batiaev.orderbook.handlers.OrderBookEventHandler;
import com.lmax.disruptor.AggregateEventHandler;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class DisruptorEventBus implements EventBus {
    public static final EventFactory<OrderBookUpdateEvent> EVENT_FACTORY = OrderBookUpdateEvent::new;
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
                new BlockingWaitStrategy());
//                new BusySpinWaitStrategy());
        if (eventHandler.length == 0)
            throw new IllegalArgumentException("should be at least one handler");
        disruptor.handleEventsWith(new AggregateEventHandler<>(eventHandler));
    }

    @Override
    public EventEnricher<OrderBookUpdateEvent> start() {
        if (ringBuffer == null) {
            synchronized (this) {
                if (ringBuffer == null) {
                    ringBuffer = disruptor.start();
                }
            }
        }
        return new DisruptorEventEnricher(ringBuffer);
    }

    @Override
    public void clean() {
        for (OrderBookEventHandler handler : eventHandler) {
            handler.clear();
        }
    }
}
