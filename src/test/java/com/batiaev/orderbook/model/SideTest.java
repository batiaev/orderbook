package com.batiaev.orderbook.model;

import org.junit.jupiter.api.Test;

import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.Side.SELL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class SideTest {
    @Test
    void should_create_buy_side() {
        //when
        Side side = Side.of("buy");
        //then
        assertEquals(BUY, side);
    }

    @Test
    void should_create_sell_side() {
        //when
        Side side = Side.of("sell");
        //then
        assertEquals(SELL, side);
    }

    @Test
    void should_throw_exception() {
        //then
        assertThrowsExactly(IllegalArgumentException.class, () -> Side.of("offer"));
    }
}
