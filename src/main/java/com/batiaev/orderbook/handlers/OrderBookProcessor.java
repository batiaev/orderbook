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

import static java.util.Objects.requireNonNull;

public class OrderBookProcessor implements OrderBookEventHandler, OrderBookHolder {
    public static final Logger logger = LoggerFactory.getLogger(OrderBookProcessor.class);
    private final int depth;
    private final Map<ProductId, OrderBook> orderBooks = new HashMap<>();
    private final Map<ProductId, Long> productUpdates = new HashMap<>();
    private final Map<ProductId, Long> subscriptionTime = new HashMap<>();
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
            default -> {
            }
        }
    }

    @Override
    public Map<String, Double> orderBooksUpdates() {
        var res = new HashMap<String, Double>();
        long now = System.currentTimeMillis();
        for (ProductId productId : orderBooks.keySet()) {
            res.put(productId.id(), productUpdates.getOrDefault(productId, 1L) / ((now - subscriptionTime.getOrDefault(productId, now)) / 1000.));
        }
        return res;
    }

    @Override
    public OrderBook orderBook(ProductId productId) {
        return orderBooks.get(productId);
    }

    @Override
    public void clean() {
        orderBooks.clear();
    }

    private OrderBook init(OrderBookUpdateEvent snapshot, long sequence) {
        logger.trace("Processed snapshot seq=" + sequence);
        orderBooks.put(snapshot.productId(), orderBookFactory.apply(snapshot, depth));
        return orderBooks.get(snapshot.productId());
    }

    private void update(OrderBookUpdateEvent update, long sequence) {
        logger.trace("Processed update seq=" + sequence);
        orderBooks.putIfAbsent(update.productId(), orderBookFactory.apply(OrderBookUpdateEvent.emtpySnapshot(update.productId()), depth));
        productUpdates.put(update.productId(), productUpdates.getOrDefault(update.productId(), 0L) + 1);
        subscriptionTime.putIfAbsent(update.productId(), System.currentTimeMillis());
        orderBooks.computeIfPresent(update.productId(), (productId, orderBook) -> orderBook.update(update));
    }

    @Override
    public List<OrderBookUpdateEvent.PriceLevel> groupBy(ProductId productId, BigDecimal group) {
        return requireNonNull(orderBooks.computeIfPresent(productId, (pid, orderBook) -> orderBook.group(group))).orderBook();
    }

    @Override
    public BigDecimal getGroup(ProductId productId) {
        return orderBooks.containsKey(productId) ? orderBooks.get(productId).getGroup() : BigDecimal.ZERO;
    }

    @Override
    public int resize(ProductId productId, int depth) {
        orderBooks.computeIfPresent(productId, (prd, orderBook) -> orderBook.resize(depth));
        return depth;
    }
}
