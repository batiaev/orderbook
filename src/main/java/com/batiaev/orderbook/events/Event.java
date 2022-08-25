package com.batiaev.orderbook.events;

public interface Event {
    Type type();

    enum Type {
        UNKNOWN,
        SUBSCRIBE,
        SNAPSHOT,
        L2UPDATE
    }
}
