package com.batiaev.orderbook.providers;

import com.batiaev.orderbook.events.OrderBookSubscribeEvent;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.eventbus.EventEnricher;
import com.batiaev.orderbook.model.TradingVenue;

import java.lang.ref.Cleaner;

public interface OrderBookProvider {
    OrderBookProvider start(OrderBookSubscribeEvent event, EventEnricher<OrderBookUpdateEvent> eventBus);

    void sendMessage(OrderBookSubscribeEvent subscribeOn);

    OrderBookProvider setStorage(Cleaner.Cleanable storage);

    TradingVenue venueName();
}
