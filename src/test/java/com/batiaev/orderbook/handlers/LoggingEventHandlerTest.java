package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.model.orderBook.OrderBook;
import com.batiaev.orderbook.model.orderBook.OrderBookFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingEventHandlerTest {
    @Test
    void should_create_log_handler() {
        var orderBookProcessor = new OrderBookProcessor(new OrderBookFactory(OrderBook.Type.TREE_MAP), 5);
        var loggingEventHandler1 = new LoggingEventHandler(orderBookProcessor, 100, true);
        var loggingEventHandler2 = new LoggingEventHandler(orderBookProcessor, 100, true);
        assertEquals(loggingEventHandler2, loggingEventHandler1);
    }
}
