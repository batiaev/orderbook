package com.batiaev.orderbook.model;

import java.math.BigDecimal;

import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;

public record TwoWayQuote(BigDecimal bid, BigDecimal offer) {
    public TwoWayQuote(BigDecimal bid, BigDecimal offer) {
        this.bid = bid;
        this.offer = offer;
        if (isInvalid())
            throw new IllegalArgumentException("bid > ask");
    }

    BigDecimal getSpread() {
        return offer.subtract(bid).abs();
    }

    @Override
    public String toString() {
        return String.format("BBO = %.2f / %.2f (%.3f)", bid, offer, getSpread());
    }

    public BigDecimal mid() {
        return offer.add(bid).divide(valueOf(2), HALF_UP);
    }

    public boolean isInvalid() {
        return offer.compareTo(bid) < 0;
    }
}
