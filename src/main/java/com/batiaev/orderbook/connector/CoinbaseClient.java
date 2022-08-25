package com.batiaev.orderbook.connector;

import com.batiaev.orderbook.events.ModelEvent;
import com.batiaev.orderbook.events.OrderBookSubscribeEvent;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.orderBook.OrderBook;
import com.batiaev.orderbook.serializer.CoinbaseModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public final static EventFactory<ModelEvent> EVENT_FACTORY = ModelEvent::new;
    private final static Map<ProductId, OrderBook> orderBooks = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new CoinbaseModule());
    private RingBuffer<ModelEvent> ringBuffer;

    private final String host;

    public CoinbaseClient(String host) {
        this.host = host;
    }

    public void start(OrderBookSubscribeEvent event, CoinbaseEventHandler eventHandler) throws IOException, WebSocketException {
        var disruptor = new Disruptor<>(EVENT_FACTORY,
                1024,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new BusySpinWaitStrategy());
        disruptor.handleEventsWith(eventHandler);
        ringBuffer = disruptor.start();

        new WebSocketFactory()
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
                        orderBooks.clear();
                    }

                    @Override
                    public void onTextMessage(WebSocket websocket, String text) {
                        long sequenceId = ringBuffer.next();
                        var event = parse(text);
                        var valueEvent = ringBuffer.get(sequenceId);
                        valueEvent.setType(event.type());
                        valueEvent.setPayload(event.payload());
                        ringBuffer.publish(sequenceId);
                    }
                })
                .connect();
    }

    private static ModelEvent parse(String text) {
        try {
            return mapper.readValue(text, ModelEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
