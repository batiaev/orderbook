package com.batiaev.orderbook.resource;

import com.batiaev.orderbook.handlers.LoggingEventHandler;
import com.batiaev.orderbook.handlers.OrderBookHolder;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.ws.WsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@WebSocket
public class OrderBookFeed {
    private static final Logger log = LoggerFactory.getLogger(LoggingEventHandler.class);

    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    private final Map<Session, ScheduledFuture<?>> openFeeds = new HashMap<>();
    private final OrderBookHolder orderBookHolder;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService executor;

    public OrderBookFeed(ObjectMapper objectMapper, OrderBookHolder orderBookHolder) {
        this.orderBookHolder = orderBookHolder;
        this.objectMapper = objectMapper;
        this.executor = Executors.newScheduledThreadPool(1);
    }

    @OnWebSocketError
    public void error(Session session, Throwable error) {
        log.info("Error: " + error);
    }

    @OnWebSocketConnect
    public void connected(Session session) {
        sessions.add(session);
        log.info("Connected");
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
        log.info("Disconnected");
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        log.info("Message: " + message);
        try {
            var request = objectMapper.readValue(message, WsRequest.class);
            if (request.getType().equals("subscribe")) {
                ScheduledFuture<?> future = executor.scheduleWithFixedDelay(() -> sendOrderBook(session, request.getProduct()), 0, 2, TimeUnit.SECONDS);
                openFeeds.put(session, future);
            } else if (request.getType().equals("unsubscribe")) {
                openFeeds.remove(session).cancel(false);
            } else {
                var orderBook = orderBookHolder.orderBook(ProductId.productId(message)).orderBook();
                var json = objectMapper.writeValueAsString(orderBook);
                session.getRemote().sendString(json);
            }
        } catch (Exception e) {
            sendOrderBook(session, message);
            log.error("Message parsing error: " + e.getMessage());
            return;
        }
    }

    private void sendOrderBook(Session session, String message) {
        var orderBook = orderBookHolder.orderBook(ProductId.productId(message)).orderBook();
        try {
            var json = objectMapper.writeValueAsString(orderBook);
            session.getRemote().sendString(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
