package com.batiaev.orderbook.model;

import java.util.regex.Pattern;

public record Currency(String id) {
    public Currency {
        boolean match = Pattern.matches("^[a-zA-Z0-9]+$", id);
        if (!match) {
            throw new IllegalArgumentException("Currency code should have one or more latin symbols or numbers (^[a-zA-Z0-9]+$) but was " + id);
        }
    }

    public static Currency currency(String id) {
        return new Currency(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
