package com.batiaev.orderbook.utils;

import com.batiaev.orderbook.model.Side;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderBookUtilsTest {
    @ParameterizedTest
    @CsvSource(value = {
            "20,0",
            "30,0",
            "35,1",
            "50,2",
            "60,3",
            "65,4",
    }, delimiter = ',')
    void should_perform_binary_search_on_ask_side(int key, int expected) {
        int result = OrderBookUtils.binarySearch(Side.SELL, new long[]{30, 3, 40, 4, 50, 5, 60, 6}, key);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "20,4",
            "30,3",
            "35,3",
            "50,1",
            "60,0",
            "65,0",
    }, delimiter = ',')
    void should_perform_binary_search_on_bid_side(int key, int expected) {
        int result = OrderBookUtils.binarySearch(Side.BUY, new long[]{60, 6, 50, 5, 40, 4, 30, 3}, key);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "1613.32, 0.1, 1613.4",
            "1613.32, 0.5, 1613.5",
            "1613.32, 1, 1614",
            "1613.32, 3, 1614",
            "1613.32, 2.5, 1615.0",
            "1613.32, 5, 1615",
            "1613.32, 10, 1620",
            "1613.32, 100, 1700",
    })
    void should_round(BigDecimal origin, BigDecimal round, BigDecimal expected) {
        //when
        BigDecimal actual = OrderBookUtils.round(origin, round);
        //then
        assertEquals(expected, actual);
    }
}
