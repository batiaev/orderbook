package com.batiaev.orderbook.resource;

import com.batiaev.orderbook.CoinbaseClient;
import com.batiaev.orderbook.handlers.GroupingEventHandler;
import com.batiaev.orderbook.handlers.OrderBookProcessor;
import com.batiaev.orderbook.model.orderBook.OrderBook;
import com.batiaev.orderbook.model.orderBook.OrderBookFactory;
import com.batiaev.orderbook.serializer.OrderBookEventParser;
import org.junit.jupiter.api.Test;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderBookApiTest {
    @Test
    void should_create_api() {
        //given
        var orderBookHolder = new OrderBookProcessor(new OrderBookFactory(OrderBook.Type.TREE_MAP), 5);
        var client = new CoinbaseClient("", orderBookHolder, new OrderBookEventParser());
        //when
        OrderBookApi api = new OrderBookApi(client, "l2update", orderBookHolder, new GroupingEventHandler(TEN));
        assertNotNull(api);
    }
}
