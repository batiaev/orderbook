package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class LoggingEventHandler implements OrderBookEventHandler {

    private final OrderBookHolder orderBookHolder;
    private static final Logger logger = LoggerFactory.getLogger(LoggingEventHandler.class);
    private final int frequency;
    private final boolean enableLogs;

    public LoggingEventHandler(OrderBookHolder orderBookHolder, int frequency, boolean enableLogs) {
        this.orderBookHolder = orderBookHolder;
        this.frequency = frequency;
        this.enableLogs = enableLogs;
    }

    @Override
    public void onEvent(OrderBookUpdateEvent event, long sequence, boolean endOfBatch) {
        if (enableLogs && sequence % frequency == 0) {
            final var orderBook = orderBookHolder.orderBook(event.productId());
            if (orderBook != null) {
//                logger.info("{}", orderBook);
                var truncatedOrderBook = orderBook.orderBook();
                var builder = new StringBuilder(System.lineSeparator());
                builder.append("SIDE    PRICE      SIZE").append(System.lineSeparator());
                if (truncatedOrderBook != null)
                    for (OrderBookUpdateEvent.PriceLevel pl : truncatedOrderBook) {
                        if (pl != null)
                            builder.append(String.format("%4s %8.2f %12.8f%n", pl.side(), pl.priceLevel(), pl.size()));
                    }
                logger.info("{}", builder);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoggingEventHandler that = (LoggingEventHandler) o;
        return frequency == that.frequency && enableLogs == that.enableLogs && Objects.equals(orderBookHolder, that.orderBookHolder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderBookHolder, frequency, enableLogs);
    }
}
