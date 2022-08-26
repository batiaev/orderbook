package com.batiaev.orderbook.events;

public interface Event {
    Type type();
    void clear();

    enum Type {
        UNKNOWN,
        SNAPSHOT,
        L2UPDATE;

        public static Type eventType(String type) {
            return switch (type) {
                case "snapshot" -> SNAPSHOT;
                case "l2update" -> L2UPDATE;
                default -> UNKNOWN;
            };
        }
    }
}
