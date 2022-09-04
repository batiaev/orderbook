package com.batiaev.orderbook.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public record Currency(String id) implements Comparable<Currency> {
    public static final Map<String, Currency> cache = new HashMap<>();

    public Currency {
        boolean match = Pattern.matches("^[a-zA-Z0-9]+$", id);
        if (!match) {
            throw new IllegalArgumentException("Currency code should have one or more latin symbols or numbers (^[a-zA-Z0-9]+$) but was " + id);
        }
    }

    public static Currency currency(String id) {
        return cache.computeIfAbsent(id, Currency::new);
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int compareTo(Currency currency) {
        return this.id.compareTo(currency.id);
    }
}
