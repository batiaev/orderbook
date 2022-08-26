package com.batiaev.orderbook.utils;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.Side;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.math.BigDecimal.valueOf;


public class OrderBookUtils {

    public static int binarySearch(double[][] a, double key) {
        int low = 0;
        int high = a.length - 1;

        while (low <= high) {
            int mid = low + high >>> 1;
            Double midVal = a[mid][0];
            int cmp = midVal.compareTo(key);
            if (cmp < 0) {
                low = mid + 1;
            } else {
                if (cmp <= 0) {
                    return mid;
                }

                high = mid - 1;
            }
        }

        return -(low + 1);
    }

    public static BigDecimal getPrice(double[][] bids, BigDecimal volume) {
        double v = volume.doubleValue();
        var bidPos = 0;
        int bidIdx = 0;
        while (bidPos < v && bidIdx < bids.length) {
            bidIdx++;
            bidPos += bids[bidIdx][1];
        }
        return valueOf(bids[bidIdx][0]);
    }

    public static Map<BigDecimal, BigDecimal> toMap(Side side, List<OrderBookUpdateEvent.PriceLevel> changes) {
        TreeMap<BigDecimal, BigDecimal> res = new TreeMap<>();
        for (OrderBookUpdateEvent.PriceLevel change : changes) {
            if (side.equals(change.side()))
                res.put(change.priceLevel(), change.size());
        }
        return res;
    }

    public static Map<BigDecimal, BigDecimal> toMap(double[][] asks) {
        TreeMap<BigDecimal, BigDecimal> res = new TreeMap<>();
        for (double[] ask : asks) {
            res.put(BigDecimal.valueOf(ask[0]), BigDecimal.valueOf(ask[1]));
        }
        return res;
    }
}
