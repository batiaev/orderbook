package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.batiaev.orderbook.events.Event.Type.L2UPDATE;
import static com.batiaev.orderbook.events.OrderBookUpdateEvent.PriceLevel.priceLevel;
import static com.batiaev.orderbook.model.ProductId.productId;
import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.TradingVenue.COINBASE;
import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClearingEventHandlerTest {
    @Test
    void should_clear_event() {
        //given
        var clearingEventHandler = new ClearingEventHandler();
        var event = new OrderBookUpdateEvent(L2UPDATE, COINBASE, productId("ETH-USD"), now(),
                List.of(priceLevel(BUY, 10, 10)));
        //when
        clearingEventHandler.onEvent(event, 1, true);
        //then
        assertEquals(new OrderBookUpdateEvent(), event);
    }
}
