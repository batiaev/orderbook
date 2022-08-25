package com.batiaev.orderbook.events;

import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.TradingVenue;

public record OrderBookSnapshotEvent(
        ProductId productId,

        double[][] asks,
        double[][] bids) implements OrderBookEvent {
    @Override
    public Type type() {
        return Type.SNAPSHOT;
    }

    public static OrderBookSnapshotEvent emtpySnapshot(ProductId productId) {
        return new OrderBookSnapshotEvent(productId, new double[0][], new double[0][]);
    }

    public TradingVenue tradingVenue() {
        return TradingVenue.COINBASE;//FIXME
    }
}
