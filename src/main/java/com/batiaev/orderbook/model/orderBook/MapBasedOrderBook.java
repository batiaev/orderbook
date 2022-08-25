package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.events.OrderBookSnapshotEvent;
import com.batiaev.orderbook.events.PriceLevel;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.Side;
import com.batiaev.orderbook.model.TradingVenue;
import com.batiaev.orderbook.model.TwoWayQuote;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.utils.OrderBookUtils.toMap;
import static java.time.Instant.now;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.naturalOrder;

public class MapBasedOrderBook implements OrderBook {
    private final TradingVenue venue;
    private final ProductId productId;
    private Instant lastUpdate;
    private final int depth;
    private final SortedMap<BigDecimal, BigDecimal> asks;
    private final SortedMap<BigDecimal, BigDecimal> bids;

    public static OrderBook orderBook(OrderBookSnapshotEvent snapshot, int depth) {
        return new MapBasedOrderBook(snapshot.tradingVenue(), snapshot.productId(), now(), depth, toMap(snapshot.bids()), toMap(snapshot.asks()));
    }

    public MapBasedOrderBook(TradingVenue venue, ProductId productId, Instant lastUpdate, int depth, Map<BigDecimal, BigDecimal> bids, Map<BigDecimal, BigDecimal> asks) {
        this.depth = depth;
        this.venue = venue;
        this.productId = productId;
        this.lastUpdate = lastUpdate;
        this.bids = new TreeMap<>(reverseOrder());
        this.bids.putAll(bids);
        this.asks = new TreeMap<>(naturalOrder());
        this.asks.putAll(asks);
    }

    @Override
    public TradingVenue getVenue() {
        return venue;
    }

    @Override
    public ProductId getProductId() {
        return productId;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public TwoWayQuote getQuote(BigDecimal volume) {
        return new TwoWayQuote(asks.firstKey(), bids.firstKey());
    }

    @Override
    public OrderBook update(ProductId productId, Instant time, List<PriceLevel> changes) {
        if (!Objects.equals(productId, this.productId))
            throw new IllegalArgumentException("Received order book update for another product " + productId);

        if (time.isBefore(lastUpdate)) {
            //skip old updates
            return this;
        }
        lastUpdate = time;
        for (PriceLevel change : changes) {
            update(change.side(), change.priceLevel(), change.size());
        }
        return this;
    }

    public void update(Side side, BigDecimal price, BigDecimal size) {
        if (side.equals(BUY)) {
            if (price.compareTo(asks.firstKey()) > 0) {
                System.out.printf("Invalid price update: bidUpdate = %s and bestAsk= %s%n", price, asks.firstKey());
                return; //should never happen
            }
            if (size.compareTo(PIPS) < 0) bids.remove(price);
            else bids.put(price, size);
        } else {
            if (price.compareTo(bids.firstKey()) < 0) {
                System.out.printf("Invalid price update: askUpdate = %s and bestBid= %s%n", price, bids.firstKey());
                return; //should never happen
            }
            if (size.compareTo(PIPS) < 0) asks.remove(price);
            else asks.put(price, size);
        }
        if (asks.firstKey().doubleValue() < bids.firstKey().doubleValue()) {
            System.out.printf("ASK < BID = %s < %s%n", asks.firstKey(), bids.firstKey());
        }
    }

    public Map<BigDecimal, BigDecimal> getBids() {
        return Map.copyOf(bids);
    }

    public Map<BigDecimal, BigDecimal> getAsks() {
        return Map.copyOf(bids);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapBasedOrderBook that = (MapBasedOrderBook) o;
        return Objects.equals(bids, that.bids) && Objects.equals(asks, that.asks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bids, asks);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(System.lineSeparator() + "  PRICE  SIZE").append(System.lineSeparator());
        List<String> askString = filteredList(asks, depth);
        List<String> bidString = filteredList(bids, depth);
        for (int i = askString.size() - 1; i >= 0; i--) {
            String string = askString.get(i);
            builder.append(string).append(System.lineSeparator());
        }
        builder.append("------------------").append(System.lineSeparator());
        for (String s : bidString) {
            builder.append(s).append(System.lineSeparator());
        }
        return builder.append(System.lineSeparator()).toString();
    }

    private List<String> filteredList(SortedMap<BigDecimal, BigDecimal> data, int depth) {
        int resultSize = Math.min(depth, data.size());
        List<String> a = new ArrayList<>(resultSize);
        int idx = 0;
        for (Map.Entry<BigDecimal, BigDecimal> entry : data.entrySet()) {
            BigDecimal price = entry.getKey();
            BigDecimal size = entry.getValue();
            if (idx >= resultSize) return a;
            a.add(String.format("%6.2f %6.2f", price, size));
            idx++;
        }
        return a;
    }
}
