package com.batiaev.orderbook;

import com.batiaev.orderbook.events.OrderBookSubscribeEvent;
import com.batiaev.orderbook.handlers.OrderBookProcessor;
import com.batiaev.orderbook.model.orderBook.OrderBook;
import com.batiaev.orderbook.model.orderBook.OrderBookFactory;
import com.batiaev.orderbook.serializer.OrderBookEventParser;
import com.neovisionaries.ws.client.WebSocketException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.batiaev.orderbook.model.ProductId.productId;

class CoinbaseClientTest {
    @Test
    void should_start_client() throws WebSocketException, IOException {
        //given
        var orderBookHolder = new OrderBookProcessor(new OrderBookFactory(OrderBook.Type.TREE_MAP), 5);
        var client = new CoinbaseClient(CoinbaseClient.HOST, orderBookHolder, new OrderBookEventParser());
        var eventBus = new DisruptorEventBus((event, sequence, endOfBatch) -> {});
        //when
        client.start(OrderBookSubscribeEvent.subscribeOn("l2update", productId("ETH-USD")), eventBus);
    }
}
