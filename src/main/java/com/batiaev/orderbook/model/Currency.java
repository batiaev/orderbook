package com.batiaev.orderbook.model;

public record Currency(String id) {

    public static Currency currency(String id) {
        return new Currency(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
