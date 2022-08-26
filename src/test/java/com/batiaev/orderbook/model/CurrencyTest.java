package com.batiaev.orderbook.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class CurrencyTest {

    @Test
    void should_create_currency() {
        //when
        String id = "ETH";
        Currency eth = new Currency(id);
        //then
        assertEquals(eth.id(), id);
    }

    @Test
    void should_throw_exception() {
        //then
        assertThrowsExactly(IllegalArgumentException.class, () -> new Currency("ETH!"));
    }
}
