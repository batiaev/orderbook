package com.batiaev.orderbook.utils;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.Side;
import com.carrotsearch.hppc.LongLongHashMap;

import java.math.BigDecimal;
import java.util.*;

import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.Side.SELL;
import static com.batiaev.orderbook.model.orderBook.LongsMapOrderBook.PRICE_MULTIPLIER;
import static com.batiaev.orderbook.model.orderBook.LongsMapOrderBook.SIZE_MULTIPLIER;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

public class OrderBookUtils {

    public static SortedMap<BigDecimal, BigDecimal> toMap(Side side, List<OrderBookUpdateEvent.PriceLevel> changes) {
        var res = new TreeMap<BigDecimal, BigDecimal>(side.equals(BUY) ? reverseOrder() : naturalOrder());
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

    public static LongLongHashMap toLongMap(Side side, List<OrderBookUpdateEvent.PriceLevel> changes, int depth) {
        var res = new LongLongHashMap();
        Comparator<OrderBookUpdateEvent.PriceLevel> comparator = side.equals(BUY)
                ? comparing(OrderBookUpdateEvent.PriceLevel::priceLevel)
                : (pl1, pl2) -> pl2.priceLevel().compareTo(pl1.priceLevel());
        var collect = changes.stream()
                .collect(groupingBy(OrderBookUpdateEvent.PriceLevel::side,
                        toCollection(() -> new TreeSet<>(comparator))));
        int idx = 0;
        if (collect.get(side) != null) {
            for (OrderBookUpdateEvent.PriceLevel priceLevel : collect.get(side)) {
                if (idx++ > depth)
                    return res;
                res.put(priceLevel.priceLevel().multiply(PRICE_MULTIPLIER).longValue(), priceLevel.size().multiply(SIZE_MULTIPLIER).longValue());
            }
        }
        return res;
    }

    public static int binarySearch(Side side, long[][] a, long key) {
        int low = 0;
        int high = a.length - 1;
        while (low <= high) {
            int mid = low + high >>> 1;
            long midVal = a[mid][0];
            if (side.equals(SELL) ? midVal < key : midVal > key) {
                low = mid + 1;
            } else {
                if (side.equals(SELL) == (midVal <= key)) {
                    return mid;
                }

                high = mid - 1;
            }
        }

        return low;
    }

}
