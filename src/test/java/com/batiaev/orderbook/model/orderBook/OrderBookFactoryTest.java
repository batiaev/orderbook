package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderBookFactoryTest {
    @Test
    void should_create_array_order_book() {
        OrderBookFactory orderBookFactory = new OrderBookFactory(OrderBook.Type.LONG_ARRAY);
        //when
        OrderBook orderBook = orderBookFactory.apply(new OrderBookUpdateEvent(), 10);
        //then
        assertEquals(LongArrayOrderBook.class, orderBook.getClass());
    }

    @Test
    void should_create_map_order_book() {
        OrderBookFactory orderBookFactory = new OrderBookFactory(OrderBook.Type.MAP_BASED);
        //when
        OrderBook orderBook = orderBookFactory.apply(new OrderBookUpdateEvent(), 10);
        //then
        assertEquals(MapBasedOrderBook.class, orderBook.getClass());
    }
}
