package com.batiaev.orderbook.model;

import com.batiaev.orderbook.model.orderBook.OrderBook;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

public abstract class OrderBookTest {

    abstract OrderBook orderBook(TradingVenue venue, ProductId productId, Instant lastUpdate, int depth,
                                 Map<BigDecimal, BigDecimal> bids, Map<BigDecimal, BigDecimal> asks);

    protected TreeMap<BigDecimal, BigDecimal> orderBook(int... data) {
        final var res = new TreeMap<BigDecimal, BigDecimal>();
        for (int i = 0; i < data.length; i += 2) {
            res.put(new BigDecimal(data[i]), new BigDecimal(data[i + 1]));
        }
        return res;
    }
}
