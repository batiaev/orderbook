package com.batiaev.orderbook.events;

import com.batiaev.orderbook.model.Order;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.Side;
import com.batiaev.orderbook.model.TradingVenue;
import com.batiaev.orderbook.utils.OrderBookUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static com.batiaev.orderbook.events.Event.Type.SNAPSHOT;
import static com.batiaev.orderbook.events.Event.Type.UNKNOWN;
import static com.batiaev.orderbook.model.TradingVenue.COINBASE;
import static java.math.BigDecimal.valueOf;
import static java.time.Instant.EPOCH;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public final class OrderBookUpdateEvent implements Event {
    private Type type;
    private TradingVenue venue;
    private ProductId productId;
    private Instant time;
    private List<PriceLevel> changes;//replace on two LongLongHashMap with bids and asks

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

    public static OrderBookUpdateEvent emtpySnapshot(ProductId productId) {
        return snapshot(productId, new PriceLevel[0]);
    }

//    public static OrderBookUpdateEvent update(ProductId productId, Instant time, PriceLevel[] priceLevels) {
//        return new OrderBookUpdateEvent(L2UPDATE, COINBASE, productId, time, asList(priceLevels));
//    }

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
                new HashSet<>(this.changes).containsAll(that.changes)
                && new HashSet<>(that.changes).containsAll(this.changes);
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

    public static final class PriceLevel implements Order, Comparable<PriceLevel> {
        private Side side;
        private BigDecimal priceLevel;
        private BigDecimal size;

        public PriceLevel(
                Side side,
                BigDecimal priceLevel,
                BigDecimal size) {
            this.side = side;
            this.priceLevel = priceLevel;
            this.size = size;
        }

        public static PriceLevel priceLevel(Side side, long priceLevel, long size) {
            return new PriceLevel(side, valueOf(priceLevel), valueOf(size));
        }

        @Override
        public String toString() {
            return "PriceLevel{" +
                    "side=" + side +
                    ", priceLevel=" + priceLevel.doubleValue() +
                    ", size=" + size.doubleValue() +
                    '}';
        }

        public PriceLevel round(BigDecimal group) {
            priceLevel = OrderBookUtils.round(priceLevel, group);
            return this;
        }

        @Override
        public int compareTo(PriceLevel that) {
            if (this.side != that.side)
                return that.side.ordinal() - this.side.ordinal();
            int level = this.priceLevel.compareTo(that.priceLevel);
            if (level != 0)
                return level;
            return this.size.compareTo(that.size);
        }

        public Side side() {
            return side;
        }

        public BigDecimal priceLevel() {
            return priceLevel;
        }

        public BigDecimal size() {
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (PriceLevel) obj;
            return Objects.equals(this.side, that.side) &&
                    Objects.equals(this.priceLevel, that.priceLevel) &&
                    Objects.equals(this.size, that.size);
        }

        @Override
        public int hashCode() {
            return Objects.hash(side, priceLevel, size);
        }

        public PriceLevel update(Side side, BigDecimal level, BigDecimal size) {
            this.side = side;
            this.priceLevel = level;
            this.size = size;
            return this;
        }
    }
}
