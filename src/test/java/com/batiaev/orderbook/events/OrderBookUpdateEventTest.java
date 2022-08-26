package com.batiaev.orderbook.events;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.batiaev.orderbook.events.Event.Type.L2UPDATE;
import static com.batiaev.orderbook.events.Event.Type.UNKNOWN;
import static com.batiaev.orderbook.events.OrderBookUpdateEvent.PriceLevel.priceLevel;
import static com.batiaev.orderbook.model.ProductId.productId;
import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.TradingVenue.COINBASE;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderBookUpdateEventTest {
    @Test
    void should_create_empty_update_event() {
        //when
        var event = new OrderBookUpdateEvent();
        //then
        assertEquals(event.changes(), emptyList());
        assertNull(event.productId());
        assertEquals(UNKNOWN, event.type());
        assertNull(event.venue());
    }

    @Test
    void should_create_update_event() {
        //when
        var now = now();
        var changes = List.of(priceLevel(BUY, 10, 5));
        var event = new OrderBookUpdateEvent(L2UPDATE, COINBASE, productId("ETH-USD"), now,
                changes);
        //then
        assertEquals(changes, event.changes());
        assertEquals(productId("ETH-USD"), event.productId());
        assertEquals(L2UPDATE, event.type());
        assertEquals(COINBASE, event.venue());
    }

    @Test
    void should_clear_update_event() {
        //given
        var now = now();
        var changes = List.of(priceLevel(BUY, 10, 5));
        var event = new OrderBookUpdateEvent(L2UPDATE, COINBASE, productId("ETH-USD"), now,
                changes);
        //when
        event.clear();
        //then
        assertEquals(event.changes(), emptyList());
        assertNull(event.productId());
        assertEquals(UNKNOWN, event.type());
        assertNull(event.venue());
    }
}
