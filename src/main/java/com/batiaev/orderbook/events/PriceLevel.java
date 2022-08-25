package com.batiaev.orderbook.events;

import com.batiaev.orderbook.model.Order;
import com.batiaev.orderbook.model.Side;

import java.math.BigDecimal;

public record PriceLevel(
        Side side,
        BigDecimal priceLevel,
        BigDecimal size
) implements Order {
}
