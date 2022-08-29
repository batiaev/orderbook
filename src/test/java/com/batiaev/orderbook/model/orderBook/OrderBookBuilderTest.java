package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.providers.CoinbaseClient;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.serializer.OrderBookEventParser;
import com.neovisionaries.ws.client.WebSocketException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.batiaev.orderbook.events.OrderBookSubscribeEvent.withEvent;
import static com.batiaev.orderbook.model.orderBook.OrderBook.Type.LONG_MAP;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderBookBuilderTest {
    @Test
    void should_build_order_book() throws WebSocketException, IOException {
        //given
        var coinbaseClient = new CoinbaseClient(new OrderBookEventParser());
        var channel = "l2update";
        var product = ProductId.productId("ETH-USD");
        //when
        var orderBook = OrderBook.basedOn(LONG_MAP)
                .withGroupingBy(0.01)
                .withDepth(5)
                .withLoggingFrequency(100)
                .subscribedOn(coinbaseClient)
                .start(withEvent(channel, product));
        //then
        assertNotNull(orderBook);
    }
}
