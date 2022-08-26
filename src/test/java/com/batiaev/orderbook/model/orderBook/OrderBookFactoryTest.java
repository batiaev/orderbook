package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderBookFactoryTest {
    @Test
    void should_create_array_order_book() {
        var orderBookFactory = new OrderBookFactory(OrderBook.Type.LONG_ARRAY);
        //when
        var orderBook = orderBookFactory.apply(new OrderBookUpdateEvent(), 10);
        //then
        assertEquals(LongArrayOrderBook.class, orderBook.getClass());
    }

    @Test
    void should_create_tree_map_order_book() {
        var orderBookFactory = new OrderBookFactory(OrderBook.Type.TREE_MAP);
        //when
        var orderBook = orderBookFactory.apply(new OrderBookUpdateEvent(), 10);
        //then
        assertEquals(TreeMapOrderBook.class, orderBook.getClass());
    }

    @Test
    void should_create_long_map_order_book() {
        var orderBookFactory = new OrderBookFactory(OrderBook.Type.LONG_MAP);
        //when
        var orderBook = orderBookFactory.apply(new OrderBookUpdateEvent(), 10);
        //then
        assertEquals(LongsMapOrderBook.class, orderBook.getClass());
    }
}
