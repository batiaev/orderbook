package com.batiaev.orderbook.model;

import org.junit.jupiter.api.Test;

import static com.batiaev.orderbook.model.ProductId.productId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class ProductIdTest {
    @Test
    void should_create_product() {
        //when
        var base = "ETH";
        var counter = "USD";
        var productId = productId(base + "-" + counter);
        //then
        assertEquals(productId.getBase().id(), base);
        assertEquals(productId.getCounter().id(), counter);
    }

    @Test
    void should_throw_exception() {
        //then
        assertThrowsExactly(IllegalArgumentException.class, () -> productId("ETHUSD"));
    }

    @Test
    void should_throw_exception2() {
        //then
        assertThrowsExactly(IllegalArgumentException.class, () -> productId("ETH-USD-BTC"));
    }
}
