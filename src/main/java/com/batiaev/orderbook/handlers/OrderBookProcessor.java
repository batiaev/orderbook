package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.orderBook.OrderBook;
import com.batiaev.orderbook.model.orderBook.OrderBookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderBookProcessor implements OrderBookEventHandler, OrderBookHolder {
    public static final Logger logger = LoggerFactory.getLogger(OrderBookProcessor.class);
    private final int depth;
    private final Map<ProductId, OrderBook> orderBooks = new HashMap<>();
    private final OrderBookFactory orderBookFactory;

    public OrderBookProcessor(OrderBookFactory orderBookFactory, int depth) {
        this.orderBookFactory = orderBookFactory;
        this.depth = depth;
    }

    @Override
    public void onEvent(OrderBookUpdateEvent event, long sequence, boolean endOfBatch) {
        switch (event.type()) {
            case SNAPSHOT -> init(event, sequence);
            case L2UPDATE -> update(event, sequence);
            default -> {}
        }
    }

    @Override
    public OrderBook orderBook(ProductId productId) {
        return orderBooks.get(productId);
    }

    @Override
    public void clear() {
        orderBooks.clear();
    }

    private OrderBook init(OrderBookUpdateEvent snapshot, long sequence) {
        logger.info("Processed snapshot seq=" + sequence);
        orderBooks.put(snapshot.productId(), orderBookFactory.apply(snapshot, depth));
        return orderBooks.get(snapshot.productId());
    }

    private void update(OrderBookUpdateEvent update, long sequence) {
        logger.trace("Processed update seq=" + sequence);
        orderBooks.computeIfPresent(update.productId(), (productId, orderBook) -> orderBook.update(update));
    }

    public List<OrderBookUpdateEvent.PriceLevel> groupBy(ProductId productId, BigDecimal group) {
        return orderBooks.computeIfPresent(productId, (pid, orderBook) -> orderBook.group(group)).orderBook();
    }
}
