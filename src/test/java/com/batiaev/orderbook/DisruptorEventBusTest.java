package com.batiaev.orderbook;

import com.batiaev.orderbook.eventbus.DisruptorEventBus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
