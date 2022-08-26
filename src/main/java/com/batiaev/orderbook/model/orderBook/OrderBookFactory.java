package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;

import java.util.function.BiFunction;

public class OrderBookFactory implements BiFunction<OrderBookUpdateEvent, Integer, OrderBook> {
    private final OrderBook.Type type;

    public static OrderBookFactory orderBookFactory(OrderBook.Type type) {
        return new OrderBookFactory(type);
    }

    public OrderBookFactory(OrderBook.Type type) {
        this.type = type;
    }

    @Override
    public OrderBook apply(OrderBookUpdateEvent event, Integer depth) {
        return switch (type) {
            case MAP_BASED -> MapBasedOrderBook.orderBook(event, depth);
            case LONG_ARRAY -> LongArrayOrderBook.orderBook(event, depth);
        };
    }
}
