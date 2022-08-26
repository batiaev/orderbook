package com.batiaev.orderbook;

import com.batiaev.orderbook.handlers.ClearingEventHandler;
import com.batiaev.orderbook.handlers.LoggingEventHandler;
import com.batiaev.orderbook.handlers.OrderBookProcessor;
import com.batiaev.orderbook.model.orderBook.OrderBook;
import com.batiaev.orderbook.model.orderBook.OrderBookFactory;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;

import static com.batiaev.orderbook.events.OrderBookSubscribeEvent.subscribeOn;
import static com.batiaev.orderbook.model.ProductId.productId;
import static java.lang.Integer.parseInt;

public class OrderBookApp {
    public static void main(String[] args) throws WebSocketException, IOException {
        var host = "wss://ws-feed.exchange.coinbase.com";
        var product = productId(args.length > 0 ? args[0] : "ETH-USD");
        int depth = args.length >= 2 ? parseInt(args[1]) : 10;
        var channel = "level2";
        var orderBookFactory = new OrderBookFactory(OrderBook.Type.LONG_ARRAY);
        var orderBookProcessor = new OrderBookProcessor(orderBookFactory, depth);
        new CoinbaseClient(host)
                .start(subscribeOn(channel, product),
                        orderBookProcessor,
                        new LoggingEventHandler(orderBookProcessor, 100),
                        new ClearingEventHandler());
    }
}
