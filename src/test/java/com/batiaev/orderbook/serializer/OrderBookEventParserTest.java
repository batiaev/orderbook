package com.batiaev.orderbook.serializer;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.batiaev.orderbook.events.Event.Type.SNAPSHOT;
import static com.batiaev.orderbook.events.OrderBookUpdateEvent.PriceLevel.priceLevel;
import static com.batiaev.orderbook.model.ProductId.productId;
import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.Side.SELL;
import static com.batiaev.orderbook.model.TradingVenue.COINBASE;
import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderBookEventParserTest {

    @Test
    void should_parse_update_event() {
        //given
        var parser = new OrderBookEventParser();
        var productId = productId("ETH-USD");
        var now = now();
        var expected = new OrderBookUpdateEvent(SNAPSHOT, COINBASE, productId, now,
                List.of(priceLevel(BUY, 10, 10), priceLevel(SELL, 20, 10)));
        var result = new OrderBookUpdateEvent();
        //when
        parser.parse(result, "{\"type\":\"snapshot\", \"product_id\": \"ETH-USD\", \"time\": \"" + now + "\", " +
                "\"asks\":[[20, 10]], \"bids\":[[10,10]]}");
        //then
        assertEquals(expected, result);
    }
}
