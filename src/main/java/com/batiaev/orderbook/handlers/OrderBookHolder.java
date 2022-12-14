package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.orderBook.OrderBook;

import java.lang.ref.Cleaner;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface OrderBookHolder extends Function<ProductId, OrderBook>, Cleaner.Cleanable {
    Map<String, Double> orderBooksUpdates();

    OrderBook orderBook(ProductId productId);

    @Override
    default OrderBook apply(ProductId productId) {
        return orderBook(productId);
    }

    List<OrderBookUpdateEvent.PriceLevel> groupBy(ProductId productId, BigDecimal group);

    BigDecimal getGroup(ProductId productId);

    int resize(ProductId productId, int depth);
}
