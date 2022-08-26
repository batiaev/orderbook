package com.batiaev.orderbook;

import com.batiaev.orderbook.events.OrderBookSubscribeEvent;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.handlers.OrderBookEventHandler;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.serializer.OrderBookEventParser;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.neovisionaries.ws.client.*;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.neovisionaries.ws.client.WebSocketExtension.PERMESSAGE_DEFLATE;

public class CoinbaseClient {

    public static final EventFactory<OrderBookUpdateEvent> EVENT_FACTORY = OrderBookUpdateEvent::new;
    private static final Map<WebSocket, List<ProductId>> connections = new HashMap<>();
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private RingBuffer<OrderBookUpdateEvent> ringBuffer;
    private static final OrderBookEventParser eventParser = new OrderBookEventParser();
    private final String host;

    public CoinbaseClient(String host) {
        this.host = host;
    }

    public CoinbaseClient start(OrderBookSubscribeEvent event, OrderBookEventHandler... eventHandler) throws IOException, WebSocketException {
        var disruptor = new Disruptor<>(EVENT_FACTORY,
                DEFAULT_BUFFER_SIZE,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new BusySpinWaitStrategy());
        if (eventHandler.length == 0)
            throw new IllegalArgumentException("should be at least one handler");
        final var handlerGroup = disruptor.handleEventsWith(eventHandler[0]);
        for (int i = 1; i < eventHandler.length; i++) {
            handlerGroup.then(eventHandler[i]);
        }
        ringBuffer = disruptor.start();

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
                        for (OrderBookEventHandler handler : eventHandler) {
                            handler.clear();
                        }
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
