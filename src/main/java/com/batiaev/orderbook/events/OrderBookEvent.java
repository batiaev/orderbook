package com.batiaev.orderbook.events;

import com.batiaev.orderbook.model.ProductId;

public interface OrderBookEvent extends Event {
    ProductId productId();
}
