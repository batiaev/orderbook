package com.batiaev.orderbook.serializer;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;

public interface EventParser {

    void parse(OrderBookUpdateEvent modelEvent, String text);
}
