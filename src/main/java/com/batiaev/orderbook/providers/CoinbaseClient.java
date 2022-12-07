package com.batiaev.orderbook.providers;

import com.batiaev.orderbook.eventbus.EventEnricher;
import com.batiaev.orderbook.events.OrderBookSubscribeEvent;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.TradingVenue;
import com.batiaev.orderbook.serializer.OrderBookEventParser;
import com.neovisionaries.ws.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.neovisionaries.ws.client.WebSocketExtension.PERMESSAGE_DEFLATE;
import static com.neovisionaries.ws.client.WebSocketState.CLOSED;
import static com.neovisionaries.ws.client.WebSocketState.CLOSING;

public class CoinbaseClient implements OrderBookProvider {
    public static final Logger logger = LoggerFactory.getLogger(CoinbaseClient.class);
    public static final String HOST = "wss://ws-feed.exchange.coinbase.com";
    public static final Cleaner.Cleanable NO_OPS = () -> {
    };
    private WebSocket websocket;
    private Cleaner.Cleanable storage = NO_OPS;
    private final Set<ProductId> products = new HashSet<>();
    private EventEnricher<OrderBookUpdateEvent> eventBus;
    private final OrderBookEventParser eventParser;
    private final String host;

    public CoinbaseClient(OrderBookEventParser eventParser) {
        this(CoinbaseClient.HOST, eventParser);
    }

    public CoinbaseClient(String host, OrderBookEventParser eventParser) {
        this.host = host;
        this.eventParser = eventParser;
    }

    @Override
    public OrderBookProvider start(OrderBookSubscribeEvent event, EventEnricher<OrderBookUpdateEvent> eventBus) {
        this.eventBus = eventBus;
        this.websocket = getConnect(event);
        products.addAll(event.productId());
        return this;
    }

    private WebSocket getConnect(OrderBookSubscribeEvent event) {
        try {
            var webSocket = getWebSocket(event);
            return webSocket.connect();
        } catch (WebSocketException | IOException e) {
            logger.error("Cannot start websocket connection", e);
            throw new RuntimeException(e);
        }
    }

    private WebSocket getWebSocket(OrderBookSubscribeEvent event) throws IOException {
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
                        if (storage != null) storage.clean();
                    }

                    @Override
                    public void onTextMessage(WebSocket websocket, String text) {
                        eventBus.nextEvent(orderBookUpdateEvent -> eventParser.parse(orderBookUpdateEvent, text));
                    }
                });
    }

    @Override
    public void sendMessage(OrderBookSubscribeEvent subscribeOn) {
        if (websocket == null) {
            websocket = getConnect(subscribeOn);
        }
        while (!websocket.isOpen()) {
            var state = websocket.getState();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (state.equals(CLOSED) || state.equals(CLOSING)) {
                try {
                    websocket = websocket.recreate();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (!products.containsAll(subscribeOn.productId()))
            websocket.sendText(subscribeOn.toJson());
    }

    @Override
    public OrderBookProvider setStorage(Cleaner.Cleanable storage) {
        this.storage = storage;
        return this;
    }

    @Override
    public TradingVenue venueName() {
        return TradingVenue.COINBASE;
    }
}
