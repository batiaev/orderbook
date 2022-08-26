package com.batiaev.orderbook.model;

public enum Side {
    BUY, SELL;

    public static Side of(String side) {
        return switch (side) {
            case "buy" -> BUY;
            case "sell" -> SELL;
            default -> throw new IllegalArgumentException("invalid side " + side);
        };
    }
}
