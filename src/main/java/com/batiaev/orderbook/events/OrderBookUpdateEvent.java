package com.batiaev.orderbook.events;

import com.batiaev.orderbook.model.ProductId;

import java.time.Instant;
import java.util.List;

public record OrderBookUpdateEvent(
        ProductId productId,
        Instant time,
        List<PriceLevel> changes
) implements OrderBookEvent {
    @Override
    public Type type() {
        return Type.L2UPDATE;
    }

}
