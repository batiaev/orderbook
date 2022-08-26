package com.batiaev.orderbook;

import com.batiaev.orderbook.handlers.ClearingEventHandler;
import com.batiaev.orderbook.handlers.LoggingEventHandler;
import com.batiaev.orderbook.handlers.OrderBookProcessor;
import com.batiaev.orderbook.model.orderBook.OrderBook;
import com.batiaev.orderbook.model.orderBook.OrderBookFactory;
import com.batiaev.orderbook.resource.OrderBookApi;
import com.batiaev.orderbook.serializer.OrderBookEventParser;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;

import static com.batiaev.orderbook.events.OrderBookSubscribeEvent.subscribeOn;
import static com.batiaev.orderbook.model.ProductId.productId;
import static com.batiaev.orderbook.model.orderBook.OrderBook.Type.*;
import static java.lang.Integer.parseInt;

public class OrderBookApp {
    public static void main(String[] args) throws WebSocketException, IOException {
        var host = "wss://ws-feed.exchange.coinbase.com";
        var product = productId(args.length > 0 ? args[0] : "ETH-USD");
        int depth = args.length >= 2 ? parseInt(args[1]) : 10;
        var channel = "level2";
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
        var orderBookFactory = new OrderBookFactory(type);
        var orderBookProcessor = new OrderBookProcessor(orderBookFactory, depth);
        var eventBus = new DisruptorEventBus(orderBookProcessor,
                new LoggingEventHandler(orderBookProcessor, 100, true),
                new ClearingEventHandler());
        CoinbaseClient client = new CoinbaseClient(host, orderBookProcessor, new OrderBookEventParser())
                .start(subscribeOn(channel, product), eventBus);
        new OrderBookApi(client, channel, orderBookProcessor).start();
    }
}
