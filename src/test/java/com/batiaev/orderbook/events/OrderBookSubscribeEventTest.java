package com.batiaev.orderbook.events;

import com.batiaev.orderbook.model.ProductId;
import org.junit.jupiter.api.Test;

import static com.batiaev.orderbook.events.OrderBookSubscribeEvent.subscribeOn;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderBookSubscribeEventTest {
    @Test
    void should_create_subscribe_event() {
        //when
        var expected = "{\"type\": \"subscribe\", \"product_ids\": [\"ETH-USD\"], \"channels\": [\"level2\"]}";
        var event = subscribeOn("level2", ProductId.productId("ETH-USD"));
        //then
        assertEquals(expected, event.toJson());
        assertEquals(event.getClass().getSimpleName() + expected, event.toString());
    }
}
