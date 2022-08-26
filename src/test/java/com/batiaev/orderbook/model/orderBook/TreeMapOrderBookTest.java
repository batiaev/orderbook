package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.model.OrderBookTest;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.TradingVenue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

class TreeMapOrderBookTest extends OrderBookTest {

    @Override
    protected OrderBook orderBook(TradingVenue venue, ProductId productId, Instant lastUpdate, int depth,
                                  Map<BigDecimal, BigDecimal> bids, Map<BigDecimal, BigDecimal> asks) {
        return new TreeMapOrderBook(venue, productId, lastUpdate, depth, bids, asks);
    }
}
