package com.batiaev.orderbook;

import com.batiaev.orderbook.eventbus.DisruptorEventBus;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.handlers.OrderBookEventHandler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorEventBusTest {
    @Test
    void should_create_disruptor() {
        var disruptorEventBus = new DisruptorEventBus((event, sequence, endOfBatch) -> {
        });
        var ringBuffer = disruptorEventBus.start();
        assertNotNull(ringBuffer);
    }

    @Test
    void should_throw_exception() {
        assertThrows(IllegalArgumentException.class, DisruptorEventBus::new);
    }

    @Test
    void should_call_clean_handler() {
        var idx = new AtomicInteger();
        var disruptorEventBus = new DisruptorEventBus(new OrderBookEventHandler() {
            @Override
            public void clear() {
                OrderBookEventHandler.super.clear();
                idx.incrementAndGet();
            }

            @Override
            public void onEvent(OrderBookUpdateEvent event, long sequence, boolean endOfBatch) {
                idx.incrementAndGet();
            }
        });
        //when
        disruptorEventBus.clean();
        //then
        assertEquals(1, idx.get());

    }
}
