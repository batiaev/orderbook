package com.batiaev.orderbook.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class TwoWayQuoteTest {
    @Test
    void should_create_quote() {
        //when
        var quote = new TwoWayQuote(new BigDecimal(10), new BigDecimal(20));
        //then
        assertEquals(new BigDecimal(10), quote.getSpread());
        assertEquals(new BigDecimal(15), quote.mid());
    }

    @Test
    void should_throw_exception() {
        //then
        assertThrowsExactly(IllegalArgumentException.class, () -> new TwoWayQuote(new BigDecimal(20), new BigDecimal(10)));
    }
}
