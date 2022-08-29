package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.batiaev.orderbook.events.Event.Type.L2UPDATE;
import static com.batiaev.orderbook.events.OrderBookUpdateEvent.PriceLevel.priceLevel;
import static com.batiaev.orderbook.model.ProductId.productId;
import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.Side.SELL;
import static com.batiaev.orderbook.model.TradingVenue.COINBASE;
import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DepthLimiterEventHandlerTest {
    @Test
    void should_limit_depth() {
        //given
        var handler = new DepthLimiterEventHandler(2);
        var event = new OrderBookUpdateEvent(L2UPDATE, COINBASE, productId("ETH-USD"), now(),
                List.of(
                        priceLevel(SELL, 100, 10),
                        priceLevel(SELL, 90, 10),
                        priceLevel(SELL, 80, 10),
                        priceLevel(SELL, 70, 10),
                        priceLevel(SELL, 60, 10),
                        priceLevel(BUY, 50, 10),
                        priceLevel(BUY, 40, 10),
                        priceLevel(BUY, 30, 10),
                        priceLevel(BUY, 20, 10),
                        priceLevel(BUY, 10, 10)
                ));
        var expected = List.of(
                priceLevel(SELL, 90, 10),
                priceLevel(SELL, 80, 10),
                priceLevel(SELL, 70, 10),
                priceLevel(SELL, 60, 10),
                priceLevel(BUY, 50, 10),
                priceLevel(BUY, 40, 10),
                priceLevel(BUY, 30, 10),
                priceLevel(BUY, 20, 10)
        );

        //when
        handler.onEvent(event, 1, true);

        //then
        assertEquals(expected, event.changes());
    }
}
