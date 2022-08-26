package com.batiaev.orderbook.utils;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.Side;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OrderBookUtils {

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
