package com.batiaev.orderbook;

import com.batiaev.orderbook.model.orderBook.OrderBook;
import com.batiaev.orderbook.resource.OrderBookApi;
import com.batiaev.orderbook.serializer.OrderBookEventParser;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;

import static com.batiaev.orderbook.events.OrderBookSubscribeEvent.withEvent;
import static com.batiaev.orderbook.model.ProductId.productId;
import static com.batiaev.orderbook.model.orderBook.OrderBook.Type.*;
import static java.lang.Integer.parseInt;

public class OrderBookApp {
    public static void main(String[] args) throws WebSocketException, IOException {
        var product = productId(args.length > 0 ? args[0] : "ETH-USD");
        int depth = args.length >= 2 ? parseInt(args[1]) : 10;
        var channel = "level2";
        var type = getType(args);
        var coinbaseClient = new CoinbaseClient(new OrderBookEventParser());

        var orderBook = OrderBook.basedOn(type)
                .withGroupingBy(0.01)
                .withDepth(depth)
                .withLoggingFrequency(100)
                .subscribedOn(coinbaseClient)
                .start(withEvent(channel, product));

        new OrderBookApi(coinbaseClient, channel, orderBook).start();
    }

    private static OrderBook.Type getType(String[] args) {
        OrderBook.Type type;
        if (args.length < 3 || args[2].equals("longmap")) {
            type = LONG_MAP;
        } else if ("array".equals(args[2])) {
            type = LONG_ARRAY;
        } else if (args[2].equals("treemap")) {
            type = TREE_MAP;
        } else {
            type = LONG_MAP;
        }
        return type;
    }
}
