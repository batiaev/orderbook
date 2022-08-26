package com.batiaev.orderbook.serializer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderBookEventParserTest {
    //TODO remove dummy test and add more on parsing logic
    @Test
    void should_create_parser() {
        //when
        var orderBookEventParser = new OrderBookEventParser();
        //then
        assertNotNull(orderBookEventParser);
    }
}
