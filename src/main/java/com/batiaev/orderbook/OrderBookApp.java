package com.batiaev.orderbook;

import com.batiaev.orderbook.handlers.ClearingEventHandler;
import com.batiaev.orderbook.handlers.LoggingEventHandler;
import com.batiaev.orderbook.handlers.OrderBookProcessor;
import com.batiaev.orderbook.model.orderBook.OrderBookFactory;
import com.batiaev.orderbook.resource.OrderBookApi;
import com.batiaev.orderbook.serializer.OrderBookEventParser;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;

import static com.batiaev.orderbook.events.OrderBookSubscribeEvent.subscribeOn;
import static com.batiaev.orderbook.model.ProductId.productId;
import static com.batiaev.orderbook.model.orderBook.OrderBook.Type.LONG_ARRAY;
import static com.batiaev.orderbook.model.orderBook.OrderBook.Type.MAP_BASED;
import static java.lang.Integer.parseInt;

public class OrderBookApp {
    public static void main(String[] args) throws WebSocketException, IOException {
        var host = "wss://ws-feed.exchange.coinbase.com";
        var product = productId(args.length > 0 ? args[0] : "ETH-USD");
        int depth = args.length >= 2 ? parseInt(args[1]) : 10;
        var channel = "level2";
        var type = args.length >= 3 && "array".equals(args[2]) ? LONG_ARRAY : MAP_BASED;
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
