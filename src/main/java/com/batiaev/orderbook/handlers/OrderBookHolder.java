package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.orderBook.OrderBook;

import java.util.function.Function;

public interface OrderBookHolder extends Function<ProductId, OrderBook> {
    OrderBook orderBook(ProductId productId);

    @Override
    default OrderBook apply(ProductId productId) {
        return orderBook(productId);
    }
}
