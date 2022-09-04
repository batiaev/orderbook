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
import static com.batiaev.orderbook.utils.OrderBookUtils.*;
import static java.time.Instant.EPOCH;
import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.naturalOrder;

public class LongArrayOrderBook implements OrderBook {
    private final TradingVenue venue;
    private final ProductId productId;
    private Instant lastUpdate;
    private int depth;
    private final long[][] bids;
    private int bidsSize;
    private final long[][] asks;
    private int asksSize;
    private BigDecimal group = BigDecimal.ZERO;

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
        this.bids = convertOrderBook(BUY, bids, this.depth * 2);
        this.asks = convertOrderBook(SELL, asks, this.depth * 2);
        this.bidsSize = this.bids.length;
        this.asksSize = this.asks.length;
    }

    private long[][] convertOrderBook(Side side, Map<BigDecimal, BigDecimal> b, int depth) {
        Comparator<BigDecimal> comparator = side.equals(BUY) ? reverseOrder() : naturalOrder();
        var tmp = new TreeMap<BigDecimal, BigDecimal>(comparator);
        tmp.putAll(b);
        int arraySize = Math.min(depth, b.size());
        var res = new long[arraySize][];
        int idx = 0;
        for (Map.Entry<BigDecimal, BigDecimal> entry : tmp.entrySet()) {
            res[idx++] = new long[]{toPrice(entry.getKey()), toSize(entry.getValue())};
            if (idx >= res.length)
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
    public OrderBook resize(int depth) {
        //FIXME add actual resize;
        this.depth = depth;
        return this;
    }

    @Override
    public OrderBook group(BigDecimal step) {
        this.group = step;
        reset();//FIXME
        return this;
    }

    @Override
    public synchronized OrderBook reset(OrderBookUpdateEvent event, int depth) {
        bidsSize = 0;
        asksSize = 0;
        lastUpdate = EPOCH;
        this.depth = depth;
        for (long[] bid : bids) {
            bid[0] = 0;
            bid[1] = 0;
        }
        for (long[] ask : asks) {
            ask[0] = 0;
            ask[1] = 0;
        }
        return update(event.productId(), event.time(), event.changes());
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
                    toPrice(change.priceLevel()),
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
        long[] cur = new long[]{price, size};
        if (side.equals(SELL)) { //process asks
            if (price < bids[0][0]) return; //should never happen
            int idx = binarySearch(side, asks, cur[0]);
            if (idx >= asks.length)
                return;
            if (cur[0] == asks[idx][0]) {
                moveRemoved(cur, idx, asks);
                return;
            }
            if (cur[1] == 0)
                return;
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
            if (price > asks[0][0]) return; //should never happen
            int idx = binarySearch(side, bids, cur[0]);
            if (idx >= bids.length)
                return;
            if (cur[0] == bids[idx][0]) {
                moveRemoved(cur, idx, bids);
                return;
            }
            if (cur[1] == 0)
                return;
            long[] tmp = new long[2];
            for (int i = idx; i < bids.length; i++) {
                if (cur[0] > bids[i][0]) {
                    tmp[0] = bids[i][0];
                    tmp[1] = bids[i][1];
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
                data[data.length - 1] = new long[]{0, 0};
            }
        } else {
            data[i][1] = cur[1];
        }
    }

    @Override
    public BigDecimal getGroup() {
        return group;
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
