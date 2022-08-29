package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.Side;
import com.batiaev.orderbook.model.TradingVenue;
import com.batiaev.orderbook.model.TwoWayQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.Side.SELL;
import static com.batiaev.orderbook.utils.OrderBookUtils.toMap;
import static java.time.Instant.EPOCH;
import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.naturalOrder;

public class TreeMapOrderBook implements OrderBook {
    public static final Logger logger = LoggerFactory.getLogger(TreeMapOrderBook.class);
    private final TradingVenue venue;
    private final ProductId productId;
    private Instant lastUpdate;
    private int depth;
    private final SortedMap<BigDecimal, BigDecimal> asks;
    private final SortedMap<BigDecimal, BigDecimal> bids;
    private BigDecimal group = BigDecimal.ZERO;

    public static OrderBook orderBook(OrderBookUpdateEvent snapshot, int depth) {
        return new TreeMapOrderBook(snapshot.venue(), snapshot.productId(), now(), depth,
                toMap(BUY, snapshot.changes()), toMap(SELL, snapshot.changes()));
    }

    public TreeMapOrderBook(TradingVenue venue, ProductId productId, Instant lastUpdate, int depth,
                            Map<BigDecimal, BigDecimal> bids, Map<BigDecimal, BigDecimal> asks) {
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
    public BigDecimal getGroup() {
        return group;
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
        return new TwoWayQuote(bids.firstKey(), asks.firstKey());
    }

    @Override
    public OrderBook resize(int depth) {
        //FIXME add actual resize;
        this.depth = depth;
        return this;
    }

    @Override
    public OrderBook group(BigDecimal step) {
        group = step;
        reset();//FIXME
        return this;
    }

    @Override
    public synchronized OrderBook reset(OrderBookUpdateEvent event, int depth) {
        asks.clear();
        bids.clear();
        lastUpdate = EPOCH;
        this.depth = depth;
        return update(event.productId(), event.time(), event.changes());
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
            update(change.side(), change.priceLevel(), change.size());
        }
        lastUpdate = time;
        return this;
    }

    private void update(Side side, BigDecimal price, BigDecimal size) {
        if (side.equals(BUY)) {
            if (price.compareTo(asks.firstKey()) > 0) {
                logger.trace("Invalid price update: bidUpdate = {} and bestAsk= {}", price, asks.firstKey());
                return; //should never happen
            }
            if (size.compareTo(PIPS) < 0) bids.remove(price);
            else {
                bids.put(price, size);
                if (bids.firstKey().compareTo(asks.firstKey()) >= 0)
                    asks.remove(asks.firstKey());
            }
        } else {
            if (price.compareTo(bids.firstKey()) < 0) {
                logger.trace("Invalid price update: askUpdate = {} and bestBid= {}", price, bids.firstKey());
                return; //should never happen
            }
            if (size.compareTo(PIPS) < 0) asks.remove(price);
            else {
                asks.put(price, size);
                if (asks.firstKey().compareTo(bids.firstKey()) <= 0)
                    bids.remove(bids.firstKey());
            }
        }
        if (asks.firstKey().doubleValue() <= bids.firstKey().doubleValue()) {
            logger.info("ASK <= BID = {} <= {}", asks.firstKey(), bids.firstKey());
        }
    }

    @Override
    public synchronized List<OrderBookUpdateEvent.PriceLevel> orderBook(int depth) {
        int size = Math.min(depth, asks.size()) + Math.min(depth, bids.size());
        final var priceLevels = new OrderBookUpdateEvent.PriceLevel[size];
        final var askString = filteredList(asks, SELL, depth);
        final var bidString = filteredList(bids, BUY, depth);
        int idx = 0;
        for (int i = askString.size() - 1; i >= 0; i--) {
            priceLevels[idx++] = askString.get(i);
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
        TreeMapOrderBook that = (TreeMapOrderBook) o;
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

    private List<OrderBookUpdateEvent.PriceLevel> filteredList(SortedMap<BigDecimal, BigDecimal> data, Side side, int depth) {
        int resultSize = Math.min(depth, data.size());
        OrderBookUpdateEvent.PriceLevel[] a = new OrderBookUpdateEvent.PriceLevel[resultSize];
        int idx = 0;
        for (Map.Entry<BigDecimal, BigDecimal> entry : data.entrySet()) {
            BigDecimal price = entry.getKey();
            BigDecimal size = entry.getValue();
            if (idx >= resultSize) return asList(a);
            a[idx] = new OrderBookUpdateEvent.PriceLevel(side, price, size);
            idx++;
        }
        return asList(a);
    }
}
