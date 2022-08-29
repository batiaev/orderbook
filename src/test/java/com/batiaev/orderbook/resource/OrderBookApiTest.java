package com.batiaev.orderbook.resource;

import com.batiaev.orderbook.providers.CoinbaseClient;
import com.batiaev.orderbook.handlers.OrderBookProcessor;
import com.batiaev.orderbook.model.orderBook.OrderBook;
import com.batiaev.orderbook.model.orderBook.OrderBookFactory;
import com.batiaev.orderbook.serializer.OrderBookEventParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderBookApiTest {
    @Test
    void should_create_api() {
        //given
        var orderBookHolder = new OrderBookProcessor(new OrderBookFactory(OrderBook.Type.TREE_MAP), 5);
        var client = new CoinbaseClient("", new OrderBookEventParser());
        //when
        var api = new OrderBookApi(client, "l2update", orderBookHolder);
        assertNotNull(api);
    }
}
