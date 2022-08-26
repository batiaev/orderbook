package com.batiaev.orderbook;

import com.batiaev.orderbook.events.OrderBookSubscribeEvent;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.serializer.OrderBookEventParser;
import com.lmax.disruptor.RingBuffer;
import com.neovisionaries.ws.client.*;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.neovisionaries.ws.client.WebSocketExtension.PERMESSAGE_DEFLATE;

public class CoinbaseClient {
    private static final Map<WebSocket, List<ProductId>> connections = new HashMap<>();
    private RingBuffer<OrderBookUpdateEvent> ringBuffer;
    private final OrderBookEventParser eventParser;
    private final String host;

    public CoinbaseClient(String host, OrderBookEventParser eventParser) {
        this.host = host;
        this.eventParser = eventParser;
    }

    public CoinbaseClient start(OrderBookSubscribeEvent event, EventBus eventBus) throws IOException, WebSocketException {
        ringBuffer = eventBus.start();
        final var connect = new WebSocketFactory()
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
                        eventBus.clear();
                        connections.remove(websocket);
                    }

                    @Override
                    public void onTextMessage(WebSocket websocket, String text) {
                        long sequenceId = ringBuffer.next();
                        eventParser.parse(ringBuffer.get(sequenceId), text);
                        ringBuffer.publish(sequenceId);
                    }
                })
                .connect();
        connections.merge(connect, event.productId(), (p1, p2) -> {
            p1.addAll(p2);
            return p1;
        });
        return this;
    }
}
