package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.Side;
import com.batiaev.orderbook.model.TradingVenue;
import com.batiaev.orderbook.model.TwoWayQuote;
import com.carrotsearch.hppc.LongLongHashMap;
import com.carrotsearch.hppc.SortedIterationLongLongHashMap;
import com.carrotsearch.hppc.cursors.LongLongCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.Side.SELL;
import static com.batiaev.orderbook.utils.OrderBookUtils.toLongMap;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static java.time.Instant.now;
import static java.util.Arrays.asList;

public class LongsMapOrderBook implements OrderBook {
    public static final BigDecimal PRICE_MULTIPLIER = valueOf(10000.);
    public static final BigDecimal SIZE_MULTIPLIER = valueOf(100000000.);
    public static final Logger logger = LoggerFactory.getLogger(LongsMapOrderBook.class);
    public static final int MAX_DEPTH = 100;
    private final TradingVenue venue;
    private final ProductId productId;
    private Instant lastUpdate;
    private final int depth;
    private final LongLongHashMap asks;
    private final LongLongHashMap bids;

    public static OrderBook orderBook(OrderBookUpdateEvent snapshot, int depth) {
        return new LongsMapOrderBook(snapshot.venue(), snapshot.productId(), now(), depth,
                toLongMap(BUY, snapshot.changes(), Math.min(depth*2, MAX_DEPTH)),
                toLongMap(SELL, snapshot.changes(), Math.min(depth*2, MAX_DEPTH)));
    }

    LongsMapOrderBook(TradingVenue venue, ProductId productId, Instant lastUpdate, int depth,
                      Map<BigDecimal, BigDecimal> bids, Map<BigDecimal, BigDecimal> asks) {
        this(venue, productId, lastUpdate, depth, toLongMap(bids), toLongMap(asks));
    }

    LongsMapOrderBook(TradingVenue venue, ProductId productId, Instant lastUpdate, int depth,
                      LongLongHashMap bids, LongLongHashMap asks) {
        this.depth = depth;
        this.venue = venue;
        this.productId = productId;
        this.lastUpdate = lastUpdate;
        this.bids = bids;
        this.asks = asks;
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
        return new TwoWayQuote(fromPrice(asks.iterator().next().key), fromSize(bids.iterator().next().key));
    }

    private long toPrice(BigDecimal price) {
        return price.multiply(PRICE_MULTIPLIER).longValue();
    }

    private long toSize(BigDecimal size) {
        return size.multiply(SIZE_MULTIPLIER).longValue();
    }

    private BigDecimal fromSize(long key) {
        return valueOf(key).setScale(8, HALF_UP).divide(SIZE_MULTIPLIER, HALF_UP);
    }

    private BigDecimal fromPrice(long key) {
        return valueOf(key).setScale(2, HALF_UP).divide(PRICE_MULTIPLIER, HALF_UP);
    }

    @Override
    public synchronized OrderBook update(ProductId productId, Instant time, List<OrderBookUpdateEvent.PriceLevel> changes) {
        if (!Objects.equals(productId, this.productId))
            throw new IllegalArgumentException("Received order book update for another product " + productId);
        if (time.isBefore(lastUpdate)) {
            //skip old updates
            return this;
        }
        for (OrderBookUpdateEvent.PriceLevel change : changes) {
            update(change.side(), toPrice(change.priceLevel()), toSize(change.size()));
        }
        lastUpdate = time;
        return this;
    }

    private void update(Side side, long price, long size) {
        long firstAsk = new SortedIterationLongLongHashMap(asks, Long::compare).iterator().next().key;
        long firstBid = new SortedIterationLongLongHashMap(bids, (x, y) -> Long.compare(y, x)).iterator().next().key;
        if (side.equals(BUY)) {
            if (price > firstAsk) {
                logger.trace("Invalid price update: bidUpdate = {} and bestAsk= {}", price, firstAsk);
                return; //should never happen
            }
            if (size <= 0)
                bids.remove(price);
            else {
                bids.put(price, size);
                firstBid = new SortedIterationLongLongHashMap(bids, (x, y) -> Long.compare(y, x)).iterator().next().key;
                if (firstBid >= firstAsk)
                    asks.remove(firstAsk);
            }
        } else {
            if (price < firstBid) {
                logger.trace("Invalid price update: askUpdate = {} and bestBid= {}", price, firstBid);
                return; //should never happen
            }
            if (size <= 0)
                asks.remove(price);
            else {
                asks.put(price, size);
                firstAsk = new SortedIterationLongLongHashMap(asks, Long::compare).iterator().next().key;
                if (firstAsk <= firstBid)
                    bids.remove(firstBid);
            }
        }
        if (firstAsk <= firstBid) {
            logger.info("ASK < BID = {} < {}", firstAsk, firstBid);
        }
    }

    @Override
    public synchronized List<OrderBookUpdateEvent.PriceLevel> orderBook(int depth) {
        int size = Math.min(depth, asks.size()) + Math.min(depth, bids.size());
        final var priceLevels = new OrderBookUpdateEvent.PriceLevel[size];
        final var askString = filteredList(new SortedIterationLongLongHashMap(asks, Long::compare), SELL, depth);
        final var bidString = filteredList(new SortedIterationLongLongHashMap(bids, (a, b) -> Long.compare(b, a)), BUY, depth);
        int idx = 0;
        for (int i = askString.size() - 1; i >= 0; i--) {
            OrderBookUpdateEvent.PriceLevel priceLevel = askString.get(i);
            priceLevels[idx++] = priceLevel;
        }
        for (OrderBookUpdateEvent.PriceLevel s : bidString) {
            priceLevels[idx++] = s;
        }
        return asList(priceLevels);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LongsMapOrderBook that = (LongsMapOrderBook) o;
        return Objects.equals(bids, that.bids) && Objects.equals(asks, that.asks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bids, asks);
    }

    @Override
    public String toString() {
        return String.format("OrderBook for %s updated at %s with %s", productId.id(), lastUpdate, getQuote());
    }

    private List<OrderBookUpdateEvent.PriceLevel> filteredList(SortedIterationLongLongHashMap data, Side side, int depth) {
        int resultSize = Math.min(depth, data.size());
        OrderBookUpdateEvent.PriceLevel[] a = new OrderBookUpdateEvent.PriceLevel[resultSize];
        int idx = 0;
        for (LongLongCursor entry : data) {
            long price = entry.key;
            long size = entry.value;
            if (idx >= resultSize) return asList(a);
            a[idx] = new OrderBookUpdateEvent.PriceLevel(side, fromPrice(price), fromSize(size));
            idx++;
        }
        return asList(a);
    }
}
