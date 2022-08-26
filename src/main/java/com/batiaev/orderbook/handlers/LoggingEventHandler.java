package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEventHandler implements OrderBookEventHandler {

    private final OrderBookHolder orderBookHolder;
    private static final Logger logger = LoggerFactory.getLogger(LoggingEventHandler.class);
    private final int frequency;

    public LoggingEventHandler(OrderBookHolder orderBookHolder, int frequency) {
        this.orderBookHolder = orderBookHolder;
        this.frequency = frequency;
    }

    @Override
    public void onEvent(OrderBookUpdateEvent event, long sequence, boolean endOfBatch) {
        if (sequence % frequency == 0) {
            final var orderBook = orderBookHolder.orderBook(event.productId());
            if (orderBook != null) {
//                logger.info("{}", orderBook);
                var truncatedOrderBook = orderBook.orderBook();
                var builder = new StringBuilder(System.lineSeparator());
                builder.append("SIDE    PRICE      SIZE").append(System.lineSeparator());
                for (OrderBookUpdateEvent.PriceLevel pl : truncatedOrderBook) {
                    builder.append(String.format("%4s %8.2f %12.8f%n", pl.side(), pl.priceLevel(), pl.size()));
                }
                logger.info("{}", builder);
            }
        }
    }
}
