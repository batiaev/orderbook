package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.CoinbaseClient;
import com.batiaev.orderbook.DisruptorEventBus;
import com.batiaev.orderbook.events.OrderBookSubscribeEvent;
import com.batiaev.orderbook.handlers.*;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.math.BigDecimal;

public class OrderBookBuilder {
    private final OrderBook.Type type;
    private BigDecimal group;
    private CoinbaseClient coinbaseClient;
    private int depth;

    public OrderBookBuilder(OrderBook.Type type) {
        this.type = type;
    }

    public OrderBookBuilder withGroupingBy(double group) {
        this.group = BigDecimal.valueOf(group);
        return this;
    }

    public OrderBookBuilder withDepth(int depth) {
        this.depth = depth;
        return this;
    }

    public OrderBookBuilder subscribedOn(CoinbaseClient coinbaseClient) {
        this.coinbaseClient = coinbaseClient;
        return this;
    }

    public OrderBookHolder start(OrderBookSubscribeEvent event) throws WebSocketException, IOException {
        var orderBookFactory = new OrderBookFactory(type);
        var orderBookProcessor = new OrderBookProcessor(orderBookFactory, depth);
        var groupingEventHandler = new GroupingEventHandler(group);
        var eventBus = new DisruptorEventBus(
                groupingEventHandler,
                orderBookProcessor,
                new LoggingEventHandler(orderBookProcessor, 100, true),
                new ClearingEventHandler());
        coinbaseClient.setOrderBookHolder(orderBookProcessor).start(event, eventBus.start());
        return orderBookProcessor;
    }
}
