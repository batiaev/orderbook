package com.batiaev.orderbook.events;

import com.batiaev.orderbook.model.Order;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.Side;
import com.batiaev.orderbook.model.TradingVenue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static com.batiaev.orderbook.events.Event.Type.*;
import static com.batiaev.orderbook.model.TradingVenue.COINBASE;
import static java.time.Instant.EPOCH;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public final class OrderBookUpdateEvent implements Event {
    private Type type;
    private TradingVenue venue;
    private ProductId productId;
    private Instant time;
    private List<PriceLevel> changes;

    public OrderBookUpdateEvent() {
        clear();
    }

    public OrderBookUpdateEvent(Type type, TradingVenue venue, ProductId productId, Instant time,
                                List<PriceLevel> changes) {
        this.type = type;
        this.venue = venue;
        this.productId = productId;
        this.time = time;
        this.changes = changes;
    }

    public static OrderBookUpdateEvent update(ProductId productId, Instant time, PriceLevel[] priceLevels) {
        return new OrderBookUpdateEvent(L2UPDATE, COINBASE, productId, time, asList(priceLevels));
    }

    public static OrderBookUpdateEvent snapshot(ProductId productId, PriceLevel[] priceLevels) {
        return new OrderBookUpdateEvent(SNAPSHOT, COINBASE, productId, EPOCH, asList(priceLevels));
    }

    @Override
    public void clear() {
        this.type = UNKNOWN;
        this.venue = null;
        this.productId = null;
        this.time = EPOCH;
        this.changes = emptyList();
    }

    public Type type() {
        return type;
    }

    public TradingVenue venue() {
        return venue;
    }

    public ProductId productId() {
        return productId;
    }

    public Instant time() {
        return time;
    }

    public List<PriceLevel> changes() {
        return changes;
    }

    public OrderBookUpdateEvent setType(Type type) {
        this.type = type;
        return this;
    }

    public OrderBookUpdateEvent setVenue(TradingVenue venue) {
        this.venue = venue;
        return this;
    }

    public OrderBookUpdateEvent setProductId(ProductId productId) {
        this.productId = productId;
        return this;
    }

    public OrderBookUpdateEvent setTime(Instant time) {
        this.time = time;
        return this;
    }

    public OrderBookUpdateEvent setChanges(List<PriceLevel> changes) {
        this.changes = changes;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (OrderBookUpdateEvent) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.venue, that.venue) &&
                Objects.equals(this.productId, that.productId) &&
                Objects.equals(this.time, that.time) &&
                Objects.equals(this.changes, that.changes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, venue, productId, time, changes);
    }

    @Override
    public String toString() {
        return "OrderBookUpdateEvent[" +
                "type=" + type + ", " +
                "venue=" + venue + ", " +
                "productId=" + productId + ", " +
                "time=" + time + ", " +
                "changes=" + changes + ']';
    }

    public record PriceLevel(
            Side side,
            BigDecimal priceLevel,
            BigDecimal size
    ) implements Order {
        public static PriceLevel priceLevel(Side side, long priceLevel, long size) {
            return new PriceLevel(side, BigDecimal.valueOf(priceLevel), BigDecimal.valueOf(size));
        }

        @Override
        public String toString() {
            return "PriceLevel{" +
                    "side=" + side +
                    ", priceLevel=" + priceLevel.doubleValue() +
                    ", size=" + size.doubleValue() +
                    '}';
        }
    }
}
