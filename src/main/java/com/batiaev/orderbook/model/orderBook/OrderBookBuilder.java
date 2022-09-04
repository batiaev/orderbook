package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.eventbus.DisruptorEventBus;
import com.batiaev.orderbook.events.OrderBookSubscribeEvent;
import com.batiaev.orderbook.handlers.*;
import com.batiaev.orderbook.model.TradingVenue;
import com.batiaev.orderbook.providers.OrderBookProvider;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class OrderBookBuilder {
    private final OrderBook.Type type;
    private final Map<TradingVenue, OrderBookProvider> providers = new HashMap<>();
    private BigDecimal group;
    private int depth;
    private int frequency = -1;

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

    public OrderBookBuilder withLoggingFrequency(int frequency) {
        this.frequency = frequency;
        return this;
    }

    public OrderBookBuilder subscribedOn(OrderBookProvider... orderBookProviders) {
        for (OrderBookProvider orderBookProvider : orderBookProviders) {
            providers.put(orderBookProvider.venueName(), orderBookProvider);
        }
        return this;
    }

    public OrderBookHolder start(OrderBookSubscribeEvent event) throws WebSocketException, IOException {
        var orderBookFactory = new OrderBookFactory(type);
        var orderBookProcessor = new OrderBookProcessor(orderBookFactory, depth);
        var groupingEventHandler = new GroupingEventHandler(group);
        var eventBus = new DisruptorEventBus(
                groupingEventHandler,
                new DepthLimiterEventHandler(depth),
                orderBookProcessor,
                new LoggingEventHandler(orderBookProcessor, Math.max(0, frequency), frequency > 0)
//                ,new ClearingEventHandler()
        );
        var eventEnricher = eventBus.start();
        providers.forEach((venue, orderBookProvider) -> {
            orderBookProvider.setStorage(orderBookProcessor).start(event, eventEnricher);
        });
        return orderBookProcessor;
    }
}
