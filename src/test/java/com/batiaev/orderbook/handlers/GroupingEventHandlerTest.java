package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static com.batiaev.orderbook.events.Event.Type.L2UPDATE;
import static com.batiaev.orderbook.events.OrderBookUpdateEvent.PriceLevel.priceLevel;
import static com.batiaev.orderbook.model.ProductId.productId;
import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.TradingVenue.COINBASE;
import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupingEventHandlerTest {
    @ParameterizedTest
    @CsvSource(value = {
            "10,  12345, 12350",
            "1,   12345, 12345",
            "0,   12345, 12345",
            "0.1, 12345, 12345.0",
            "0.01,12345, 12345.00",
    })
    void should_group_changes_properly(BigDecimal group, long level, BigDecimal expLevel) {
        //given
        var groupingEventHandler = new GroupingEventHandler(group);
        var now = now();
        var event = new OrderBookUpdateEvent(L2UPDATE, COINBASE, productId("ETH-USD"), now,
                List.of(priceLevel(BUY, level, 10)));
        var expected = new OrderBookUpdateEvent(L2UPDATE, COINBASE, productId("ETH-USD"), now,
                List.of(new OrderBookUpdateEvent.PriceLevel(BUY, expLevel, BigDecimal.valueOf(10))));
        //when
        groupingEventHandler.onEvent(event, 1, true);
        //then
        assertEquals(expected, event);
    }
}
