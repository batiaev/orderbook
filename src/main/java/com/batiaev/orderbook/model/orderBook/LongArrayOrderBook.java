package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.Side;
import com.batiaev.orderbook.model.TradingVenue;
import com.batiaev.orderbook.model.TwoWayQuote;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.Side.SELL;
import static com.batiaev.orderbook.utils.OrderBookUtils.toMap;
import static java.math.BigDecimal.valueOf;
import static java.time.Instant.now;
import static java.util.Arrays.asList;

public class LongArrayOrderBook implements OrderBook {
    public static final BigDecimal PRICE_MULTIPLIER = valueOf(10000.);
    public static final BigDecimal SIZE_MULTIPLIER = valueOf(100000000.);
    public static final long[] EMPTY = {0, 0};
    private final TradingVenue venue;
    private final ProductId productId;
    private Instant lastUpdate;
    private final int depth;
    private final long[][] bids;
    private int bidsSize;
    private final long[][] asks;
    private int asksSize;

    public static OrderBook orderBook(OrderBookUpdateEvent snapshot, int depth) {
        return new LongArrayOrderBook(snapshot.venue(), snapshot.productId(), now(), depth,
                toMap(BUY, snapshot.changes()), toMap(SELL, snapshot.changes()));
    }

    public LongArrayOrderBook(TradingVenue venue, ProductId productId, Instant lastUpdate, int depth,
                              Map<BigDecimal, BigDecimal> bids, Map<BigDecimal, BigDecimal> asks) {
        if (depth > 100)
            throw new IllegalArgumentException("Array based order book should be max 100 levels, not " + depth);
        this.depth = depth;
        this.venue = venue;
        this.productId = productId;
        this.lastUpdate = lastUpdate;
        this.bids = convertOrderBook(BUY, new TreeMap<>(bids), this.depth + 1);
        this.asks = convertOrderBook(SELL, new TreeMap<>(asks), this.depth + 1);
        this.bidsSize = this.bids.length;
        this.asksSize = this.asks.length;
    }

    private long[][] convertOrderBook(Side side, Map<BigDecimal, BigDecimal> b, int depth) {
        int arraySize = Math.min(depth, b.size());
        var res = new long[arraySize][];
        int idx = side.equals(SELL) ? 0 : res.length - 1;
        for (Map.Entry<BigDecimal, BigDecimal> entry : b.entrySet()) {
            BigDecimal level = entry.getKey();
            BigDecimal size = entry.getValue();
            res[side.equals(SELL) ? idx++ : idx--] = new long[]{level.multiply(PRICE_MULTIPLIER).longValue(), size.multiply(SIZE_MULTIPLIER).longValue()};
            if (idx < 0 || idx >= res.length)
                return res;
        }
        return res;
    }

    @Override
    public TwoWayQuote getQuote(BigDecimal volume) {
        var bestbid = BigDecimal.valueOf(bids[0][0] / PRICE_MULTIPLIER.doubleValue());
        var bestask = BigDecimal.valueOf(asks[0][0] / PRICE_MULTIPLIER.doubleValue());
        return new TwoWayQuote(bestbid, bestask);
    }

    @Override
    public OrderBook update(ProductId productId, Instant time, List<OrderBookUpdateEvent.PriceLevel> changes) {
        if (!Objects.equals(productId, this.productId))
            throw new IllegalArgumentException("Received order book update for another product " + productId);
        if (time.isBefore(lastUpdate)) {
            //skip old updates
            return this;
        }
        for (OrderBookUpdateEvent.PriceLevel change : changes) {
            update(change.side(),
                    change.priceLevel().multiply(PRICE_MULTIPLIER).longValue(),
                    change.size().multiply(SIZE_MULTIPLIER).longValue());
        }
        bidsSize = updateSize(bids);
        asksSize = updateSize(asks);
        lastUpdate = time;
        return this;
    }

    private int updateSize(long[][] bids) {
        for (int i = bids.length - 1; i >= 0; i--) {
            if (bids[i][1] != 0)
                return i + 1;
        }
        return 0;
    }

    @Override
    public synchronized List<OrderBookUpdateEvent.PriceLevel> orderBook(int depth) {
        int size = Math.min(depth * 2, asksSize + bidsSize);
        final var priceLevels = new OrderBookUpdateEvent.PriceLevel[size];
        final var askString = filteredList(asks, asksSize, SELL, depth);
        final var bidString = filteredList(bids, bidsSize, BUY, depth);
        int idx = 0;
        for (int i = askString.size() - 1; i >= 0; i--) {
            priceLevels[idx++] = askString.get(i);
        }
        for (OrderBookUpdateEvent.PriceLevel s : bidString) {
            priceLevels[idx++] = s;
        }
        return asList(priceLevels);
    }

    private List<OrderBookUpdateEvent.PriceLevel> filteredList(long[][] data, int dataSize, Side side, int depth) {
        int resultSize = Math.min(depth, dataSize);
        OrderBookUpdateEvent.PriceLevel[] a = new OrderBookUpdateEvent.PriceLevel[resultSize];
        int idx = 0;
        for (long[] entry : data) {
            var price = BigDecimal.valueOf(entry[0] / PRICE_MULTIPLIER.doubleValue());
            var size = BigDecimal.valueOf(entry[1] / SIZE_MULTIPLIER.doubleValue());
            if (idx >= resultSize) return asList(a);
            a[idx] = new OrderBookUpdateEvent.PriceLevel(side, price, size);
            idx++;
        }
        return asList(a);
    }

    public void update(Side side, long price, long size) {
        if (side.equals(SELL)) {
            long[] cur = new long[]{price, size};
            //TODO replace O(n) to binary search O(logN)
            for (int i = asks.length - 1; i >= 0; i--) {
                if (cur[0] == asks[i][0]) {
                    moveRemoved(cur, i, asks);
                    return;
                } else if (cur[0] > asks[i][0])
                    break;
            }
            for (int i = 0; i < asks.length; i++) {
                if (cur[0] < asks[i][0]) {
                    long[] tmp = new long[]{asks[i][0], asks[i][1]};
                    asks[i][0] = cur[0];
                    asks[i][1] = cur[1];
                    cur[0] = tmp[0];
                    cur[1] = tmp[1];
                }
            }
            if (asks[0][0] <= bids[0][0]) {
                int idx1 = 0;
                int idx2 = 0;
                while (idx2 < bids.length) {
                    if (bids[idx2][0] < asks[0][0]) {
                        bids[idx1][0] = bids[idx2][0];
                        bids[idx1][1] = bids[idx2][1];
                        idx1++;
                    }
                    idx2++;
                }
                while (idx1 < bids.length) {
                    bids[idx1][0] = 0;
                    bids[idx1][1] = 0;
                    idx1++;
                }
            }
        } else {
            long[] cur = new long[]{price, size};
            //TODO replace O(n) to binary search O(logN)
            for (int i = bids.length - 1; i >= 0; i--) {
                if (cur[0] == bids[i][0]) {
                    moveRemoved(cur, i, bids);
                    return;
                } else if (cur[0] < bids[i][0])
                    break;
            }
            for (int i = 0; i < bids.length; i++) {
                if (cur[0] > bids[i][0]) {
                    long[] tmp = new long[]{bids[i][0], bids[i][1]};
                    bids[i][0] = cur[0];
                    bids[i][1] = cur[1];
                    cur[0] = tmp[0];
                    cur[1] = tmp[1];
                }
            }
            if (bids[0][0] >= asks[0][0]) {
                int idx1 = 0;
                int idx2 = 0;
                while (idx2 < asks.length) {
                    if (bids[0][0] >= asks[idx2][0]) {
                        idx2++;
                        continue;
                    }
                    asks[idx1][0] = asks[idx2][0];
                    asks[idx1][1] = asks[idx2][1];
                    idx1++;
                    idx2++;
                }
                while (idx1 < asks.length) {
                    asks[idx1][0] = 0;
                    asks[idx1][1] = 0;
                    idx1++;
                }
            }
        }
    }

    private void moveRemoved(long[] cur, int i, long[][] data) {
        if (cur[1] == 0) {
            if (data.length - (i + 1) >= 0) {
                System.arraycopy(data, i + 1, data, i + 1 - 1, data.length - (i + 1));
                data[data.length - 1] = EMPTY;
            }
        } else {
            data[i][1] = cur[1];
        }
    }

    public TradingVenue getVenue() {
        return venue;
    }

    public ProductId getProductId() {
        return productId;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LongArrayOrderBook that = (LongArrayOrderBook) o;
        return Arrays.equals(bids, that.bids) && Arrays.equals(asks, that.asks);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(bids);
        result = 31 * result + Arrays.hashCode(asks);
        return result;
    }

    @Override
    public String toString() {
        return String.format("OrderBook for %s updated at %s with %s", productId.id(), lastUpdate, getQuote());
    }
}
