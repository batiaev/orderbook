package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.eventbus.EventEnricher;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.lmax.disruptor.RingBuffer;

import java.util.function.Consumer;

public class DisruptorEventEnricher implements EventEnricher<OrderBookUpdateEvent> {

    private final RingBuffer<OrderBookUpdateEvent> ringBuffer;

    public DisruptorEventEnricher(RingBuffer<OrderBookUpdateEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    @Override
    public void nextEvent(Consumer<OrderBookUpdateEvent> enricher) {
        long sequenceId = ringBuffer.next();
        enricher.accept(ringBuffer.get(sequenceId));
        ringBuffer.publish(sequenceId);
    }
}
