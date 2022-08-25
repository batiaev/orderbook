package com.batiaev.orderbook.model;

import java.math.BigDecimal;

public interface Order {
    Side side();

    BigDecimal priceLevel();

    BigDecimal size();

}
