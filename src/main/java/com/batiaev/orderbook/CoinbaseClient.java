package com.batiaev.orderbook;

import com.batiaev.orderbook.events.OrderBookSubscribeEvent;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.handlers.OrderBookHolder;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.serializer.OrderBookEventParser;
import com.lmax.disruptor.RingBuffer;
import com.neovisionaries.ws.client.*;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.neovisionaries.ws.client.WebSocketExtension.PERMESSAGE_DEFLATE;

public class CoinbaseClient {
    public static final String HOST = "wss://ws-feed.exchange.coinbase.com";
    private WebSocket websocket;
    private OrderBookHolder orderBookHolder;
    private final Set<ProductId> products = new HashSet<>();
    private RingBuffer<OrderBookUpdateEvent> ringBuffer;
    private final OrderBookEventParser eventParser;
    private final String host;

    public CoinbaseClient(OrderBookEventParser eventParser) {
        this(CoinbaseClient.HOST, eventParser);
    }

    public CoinbaseClient(String host, OrderBookEventParser eventParser) {
        this.host = host;
        this.eventParser = eventParser;
    }

    public CoinbaseClient start(OrderBookSubscribeEvent event,
                                RingBuffer<OrderBookUpdateEvent> ringBuffer) throws IOException, WebSocketException {
        if (orderBookHolder == null)
            throw new IllegalStateException("order book holder should be specified before starting the feed");
        this.ringBuffer = ringBuffer;
        this.websocket = getConnect(event);
        products.addAll(event.productId());
        return this;
    }

    private WebSocket getConnect(OrderBookSubscribeEvent event) throws WebSocketException, IOException {
        return new WebSocketFactory()
                .setConnectionTimeout(55000)
                .createSocket(URI.create(host))
                .addExtension(PERMESSAGE_DEFLATE)
                .addListener(new WebSocketAdapter() {
                    @Override
                    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                        websocket.sendText(event.toJson());
                    }

                    @Override
                    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
                                               WebSocketFrame clientCloseFrame, boolean closedByServer) {
                        if (orderBookHolder != null)
                            orderBookHolder.clear();
                    }

                    @Override
                    public void onTextMessage(WebSocket websocket, String text) {
                        long sequenceId = ringBuffer.next();
                        eventParser.parse(ringBuffer.get(sequenceId), text);
                        ringBuffer.publish(sequenceId);
                    }
                })
                .connect();
    }

    public void sendMessage(OrderBookSubscribeEvent subscribeOn) {
        if (websocket != null && websocket.isOpen() && !products.containsAll(subscribeOn.productId())) {
            websocket.sendText(subscribeOn.toJson());
        }
    }

    public CoinbaseClient setOrderBookHolder(OrderBookHolder orderBookHolder) {
        this.orderBookHolder = orderBookHolder;
        return this;
    }
}
