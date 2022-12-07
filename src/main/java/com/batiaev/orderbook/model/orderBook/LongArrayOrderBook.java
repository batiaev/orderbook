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
import static java.lang.Math.min;
import static java.lang.System.arraycopy;
import static java.math.BigDecimal.valueOf;
import static java.time.Instant.EPOCH;
import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static java.util.Arrays.fill;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.naturalOrder;

public class LongArrayOrderBook implements OrderBook {
    private final TradingVenue venue;
    private final ProductId productId;
    private Instant lastUpdate;
    private int depth;
    private final long[] bids;
    private int bidsSize;
    private final long[] asks;
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
        this.bidsSize = updateSize(this.bids);
        this.asksSize = updateSize(this.asks);
    }

    private long[] convertOrderBook(Side side, Map<BigDecimal, BigDecimal> b, int depth) {
        Comparator<BigDecimal> comparator = side.equals(BUY) ? reverseOrder() : naturalOrder();
        var tmp = new TreeMap<BigDecimal, BigDecimal>(comparator);
        tmp.putAll(b);
        int arraySize = b.isEmpty() ? depth : min(depth, b.size());
        var res = new long[arraySize * 2];
        int idx = 0;
        for (Map.Entry<BigDecimal, BigDecimal> entry : tmp.entrySet()) {
            res[idx++] = toPrice(entry.getKey());
            res[idx++] = toSize(entry.getValue());
            if (idx >= res.length)
                return res;
        }
        return res;
    }

    @Override
    public TwoWayQuote getQuote(BigDecimal volume) {
        var bestbid = valueOf(bids[0] / PRICE_MULTIPLIER.doubleValue());
        var bestask = valueOf(asks[0] / PRICE_MULTIPLIER.doubleValue());
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
        fill(bids, 0);
        fill(asks, 0);
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
                    toSize(change.size()));
        }
        bidsSize = updateSize(bids);
        asksSize = updateSize(asks);
        lastUpdate = time;
        return this;
    }

    private int updateSize(long[] data) {
        for (int i = data.length - 2; i >= 0; i -= 2) {
            if (data[i] != 0)
                return i / 2 + 1;
        }
        return 0;
    }

    @Override
    public synchronized List<OrderBookUpdateEvent.PriceLevel> orderBook(int depth) {
        int size = min(depth * 2, asksSize + bidsSize);
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

    private List<OrderBookUpdateEvent.PriceLevel> filteredList(long[] data, int dataSize, Side side, int depth) {
        int resultSize = min(depth, dataSize);
        var a = new OrderBookUpdateEvent.PriceLevel[resultSize];
        int idx = 0;
        for (int i = 0, dataLength = data.length - 1; i < dataLength; i += 2) {
            var price = valueOf(data[i] / PRICE_MULTIPLIER.doubleValue());
            var size = valueOf(data[i + 1] / SIZE_MULTIPLIER.doubleValue());
            if (idx >= resultSize) return asList(a);
            a[idx++] = new OrderBookUpdateEvent.PriceLevel(side, price, size);
        }
        return asList(a);
    }

    public void update(Side side, long price, long size) {
        long curPrice = price;
        long curSize = size;
        long tmpPrice;
        long tmpSize;
        if (side.equals(SELL)) { //process asks
            if (curPrice < bids[0]) return; //should never happen
            int idx = binarySearch(side, asks, curPrice, asksSize * 2);
            if (idx >= asks.length)
                return;
            if (curPrice == asks[idx]) {
                moveRemoved(curSize, idx, asks);
                return;
            }
            if (curSize == 0)
                return;
            for (int i = 0; i < asks.length - 1 && curPrice != 0; i += 2) {
                if (asks[i] == 0 || curPrice < asks[i]) {
                    tmpPrice = asks[i];
                    tmpSize = asks[i + 1];
                    asks[i] = curPrice;
                    asks[i + 1] = curSize;
                    curPrice = tmpPrice;
                    curSize = tmpSize;
                }
            }
            if (asks[0] <= bids[0]) {
                int idx1 = 0;
                int idx2 = 0;
                while (idx2 < bids.length) {
                    if (bids[idx2] < asks[0]) {
                        bids[idx1] = bids[idx2];
                        bids[idx1 + 1] = bids[idx2 + 1];
                        idx1 += 2;
                    }
                    idx2 += 2;
                }
                while (idx1 < bids.length) {
                    bids[idx1] = 0;
                    bids[idx1 + 1] = 0;
                    idx1 += 2;
                }
            }
        } else {
            if (asks[0] != 0 && curPrice > asks[0]) return; //should never happen
            int idx = binarySearch(side, bids, curPrice, bidsSize * 2);
            if (idx >= bids.length)
                return;
            if (curPrice == bids[idx]) {
                moveRemoved(curSize, idx, bids);
                return;
            }
            if (curSize == 0)
                return;
            for (int i = idx; i < bids.length - 1 && curPrice != 0; i += 2) {
                if (bids[i] == 0 || curPrice > bids[i]) {
                    tmpPrice = bids[i];
                    tmpSize = bids[i + 1];
                    bids[i] = curPrice;
                    bids[i + 1] = curSize;
                    curPrice = tmpPrice;
                    curSize = tmpSize;
                }
            }
            if (bids[0] >= asks[0]) {
                int idx1 = 0;
                int idx2 = 0;
                while (idx2 < asks.length) {
                    if (bids[0] >= asks[idx2]) {
                        idx2 += 2;
                        continue;
                    }
                    asks[idx1] = asks[idx2];
                    asks[idx1 + 1] = asks[idx2 + 1];
                    idx1 += 2;
                    idx2 += 2;
                }
                while (idx1 < asks.length) {
                    asks[idx1] = 0;
                    asks[idx1 + 1] = 0;
                    idx1 += 2;
                }
            }
        }
    }

    private void moveRemoved(long curSize, int i, long[] data) {
        if (curSize == 0) {
            if (data.length - (i + 2) >= 0) {
                arraycopy(data, i + 2, data, i + 2 - 2, data.length - (i + 2));
                data[data.length - 1] = 0L;
                data[data.length - 2] = 0L;
            }
        } else {
            data[i + 1] = curSize;
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
