package com.batiaev.orderbook.model;

import com.batiaev.orderbook.model.orderBook.LongArrayOrderBook;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LongArrayOrderBookTest {

    @Test
    void push_back_bids() {
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
        LongArrayOrderBook level2Model = new LongArrayOrderBook(
                new long[][]{{30, 10}, {20, 10}, {10, 10}},
                new long[][]{{50, 10}, {60, 10}, {70, 10}}
        );
        var expected = new LongArrayOrderBook(
                new long[][]{{40, 5}, {30, 10}, {20, 10}},
                new long[][]{{50, 10}, {60, 10}, {70, 10}}
        );
        //when
        level2Model.update(Side.BUY, 40, 5);

        //then
        assertEquals(expected.toString(), level2Model.toString());
    }

    @Test
    void pusb_back_asks() {
        /**
         * BID PRICE ASK
         *      70   10
         *      60   10
         *      50   10
         *      (40 +5)
         * 10   30
         * 10   20
         * 10   10
         */
        LongArrayOrderBook level2Model = new LongArrayOrderBook(
                new long[][]{{30, 10}, {20, 10}, {10, 10}},
                new long[][]{{50, 10}, {60, 10}, {70, 10}}
        );
        var expected = new LongArrayOrderBook(
                new long[][]{{30, 10}, {20, 10}, {10, 10}},
                new long[][]{{40, 5}, {50, 10}, {60, 10}}
        );
        //when
        level2Model.update(Side.SELL, 40, 5);

        //then
        assertEquals(expected.toString(), level2Model.toString());
    }
}
