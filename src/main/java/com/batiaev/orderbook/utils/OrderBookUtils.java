package com.batiaev.orderbook.utils;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.Side;
import com.carrotsearch.hppc.LongLongHashMap;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.batiaev.orderbook.model.orderBook.LongsMapOrderBook.PRICE_MULTIPLIER;
import static com.batiaev.orderbook.model.orderBook.LongsMapOrderBook.SIZE_MULTIPLIER;

public class OrderBookUtils {

    public static Map<BigDecimal, BigDecimal> toMap(Side side, List<OrderBookUpdateEvent.PriceLevel> changes) {
        TreeMap<BigDecimal, BigDecimal> res = new TreeMap<>();
        for (OrderBookUpdateEvent.PriceLevel change : changes) {
            if (side.equals(change.side()))
                res.put(change.priceLevel(), change.size());
        }
        return res;
    }

    public static LongLongHashMap toLongMap(Map<BigDecimal, BigDecimal> asks) {
        var res = new LongLongHashMap();
        for (Map.Entry<BigDecimal, BigDecimal> entry : asks.entrySet()) {
            res.put(entry.getKey().multiply(PRICE_MULTIPLIER).longValue(), entry.getValue().multiply(SIZE_MULTIPLIER).longValue());
        }
        return res;
    }

    public static LongLongHashMap toLongMap(Side side, List<OrderBookUpdateEvent.PriceLevel> changes) {
        var res = new LongLongHashMap();
        for (OrderBookUpdateEvent.PriceLevel change : changes) {
            if (side.equals(change.side()))
                res.put(change.priceLevel().multiply(PRICE_MULTIPLIER).longValue(), change.size().multiply(SIZE_MULTIPLIER).longValue());
        }
        return res;
    }
}
