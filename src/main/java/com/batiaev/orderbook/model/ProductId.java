package com.batiaev.orderbook.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.batiaev.orderbook.model.Currency.currency;

public class ProductId {
    public static final Map<String, ProductId> cache = new HashMap<>();
    private final Currency base;
    private final Currency counter;

    private ProductId(String id) {
        String[] split = id.split("-");
        if (split.length == 1)
            throw new IllegalArgumentException("product id should have separator -");
        if (split.length != 2)
            throw new IllegalArgumentException("Invalid product id " + id + " should have format <baseCurrency>-<counterCurrency> e.g. ETH-USD");
        base = currency(split[0]);
        counter = currency(split[1]);
    }

    public static ProductId productId(String id) {
        return cache.computeIfAbsent(id, ProductId::new);
    }

    public String id() {
        return base.id() + "-" + counter.id();
    }

    public Currency getBase() {
        return base;
    }

    public Currency getCounter() {
        return counter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductId productId = (ProductId) o;
        return Objects.equals(base, productId.base) && Objects.equals(counter, productId.counter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, counter);
    }

    @Override
    public String toString() {
        return "ProductId{" + base + "/" + counter + '}';
    }
}
