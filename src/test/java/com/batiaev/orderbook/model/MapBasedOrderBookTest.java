package com.batiaev.orderbook.model;

import com.batiaev.orderbook.model.orderBook.MapBasedOrderBook;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

import static com.batiaev.orderbook.model.ProductId.productId;
import static com.batiaev.orderbook.model.TradingVenue.COINBASE;
import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MapBasedOrderBookTest {
    @Test
    void name() {
        /**
         * BID PRICE ASK
         *      70   10
         *      60   10
         *      50   10
         * (+5 40)
         * 10   30
         * 10   20
         * 10   10
         */
        MapBasedOrderBook level2Model = new MapBasedOrderBook(COINBASE, productId("ETH-USD"), now(), 3,
                new TreeMap<>(Map.of(decimal(30), decimal(10), decimal(20), decimal(10), decimal(10), decimal(10))),
                new TreeMap<>(Map.of(decimal(50), decimal(10), decimal(60), decimal(10), decimal(70), decimal(10))));
        var expected = new MapBasedOrderBook(COINBASE, productId("ETH-USD"), now(), 3,
                new TreeMap<>(Map.of(decimal(40), decimal(5), decimal(30), decimal(10), decimal(20), decimal(10))),
                new TreeMap<>(Map.of(decimal(50), decimal(10), decimal(60), decimal(10), decimal(70), decimal(10))));
        //when
        level2Model.update(Side.BUY, decimal(40), decimal(5));

        System.out.println(level2Model.toString());
        //then
        assertEquals(expected.toString(), level2Model.toString());
    }

    private static BigDecimal decimal(int v) {
        return new BigDecimal(v);
    }
}
