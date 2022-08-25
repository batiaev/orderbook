package com.batiaev.orderbook;

import com.batiaev.orderbook.connector.CoinbaseClient;
import com.batiaev.orderbook.connector.CoinbaseEventHandler;
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
        new CoinbaseClient(host)
                .start(subscribeOn(product, channel), new CoinbaseEventHandler(depth, sequence -> sequence % 100 == 0));
    }
}
