package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;

import java.util.function.BiFunction;

public class OrderBookFactory implements BiFunction<OrderBookUpdateEvent, Integer, OrderBook> {
    private final OrderBook.Type type;

    public OrderBookFactory(OrderBook.Type type) {
        this.type = type;
    }

    @Override
    public OrderBook apply(OrderBookUpdateEvent event, Integer depth) {
        return switch (type) {
            case TREE_MAP -> TreeMapOrderBook.orderBook(event, depth);
            case LONG_MAP -> LongsMapOrderBook.orderBook(event, depth);
            case LONG_ARRAY -> LongArrayOrderBook.orderBook(event, depth);
        };
    }
}
