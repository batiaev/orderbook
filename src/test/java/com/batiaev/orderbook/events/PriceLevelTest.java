package com.batiaev.orderbook.events;

import org.junit.jupiter.api.Test;

import static com.batiaev.orderbook.events.OrderBookUpdateEvent.PriceLevel.priceLevel;
import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.Side.SELL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceLevelTest {
    @Test
    void should_compare_price_levels_by_side() {
        //given
        var pl1 = priceLevel(BUY, 10, 10);
        var pl2 = priceLevel(SELL, 10, 10);
        //when
        int res = pl1.compareTo(pl2);
        //then
        assertEquals(1, res);
    }

    @Test
    void should_compare_price_levels_by_price() {
        //given
        var pl1 = priceLevel(BUY, 10, 10);
        var pl2 = priceLevel(BUY, 20, 10);
        //when
        int res = pl1.compareTo(pl2);
        //then
        assertEquals(-1, res);
    }

    @Test
    void should_compare_price_levels_by_volume() {
        //given
        var pl1 = priceLevel(BUY, 10, 10);
        var pl2 = priceLevel(BUY, 10, 20);
        //when
        int res = pl1.compareTo(pl2);
        //then
        assertEquals(-1, res);
    }
}
