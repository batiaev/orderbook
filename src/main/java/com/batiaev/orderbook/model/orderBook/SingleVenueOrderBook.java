package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.events.OrderBookSnapshotEvent;
import com.batiaev.orderbook.events.PriceLevel;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.Side;
import com.batiaev.orderbook.model.TradingVenue;
import com.batiaev.orderbook.model.TwoWayQuote;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.Side.SELL;
import static com.batiaev.orderbook.utils.OrderBookUtils.binarySearch;
import static com.batiaev.orderbook.utils.OrderBookUtils.getPrice;
import static java.time.Instant.now;
import static java.util.Comparator.comparingDouble;

public final class SingleVenueOrderBook implements OrderBook {
    private final TradingVenue venue;
    private final ProductId productId;
    private Instant lastUpdate;
    private final double[][] bids;
    private final double[][] asks;
    private final int depth;

    public SingleVenueOrderBook(TradingVenue venue, ProductId productId, Instant lastUpdate, double[][] asks, double[][] bids, int depth) {
        this.venue = venue;
        this.productId = productId;
        this.lastUpdate = lastUpdate;
        this.asks = trim(BUY, asks, depth + 1);
        this.bids = trim(SELL, bids, depth + 1);
        this.depth = depth;
    }

    public static SingleVenueOrderBook orderBook(OrderBookSnapshotEvent snapshot, int depth) {
        return new SingleVenueOrderBook(snapshot.tradingVenue(), snapshot.productId(), now(), snapshot.bids(), snapshot.asks(), depth);
    }

    private static double[][] trim(Side side, double[][] priceLevels, int depth) {
        if (side.equals(BUY)) {
            Arrays.sort(priceLevels, comparingDouble(d -> d[0]));
        } else {
            Arrays.sort(priceLevels, (d1, d2) -> Double.compare(d2[0], d1[0]));
        }
        if (priceLevels.length != depth) {
            double[][] res = new double[depth][2];
            for (int i = 0; i < res.length && i < priceLevels.length; i++) {
                res[i] = new double[]{priceLevels[i][0], priceLevels[i][1]};
            }
            return res;
        }
        return priceLevels;
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
            switch (change.side()) {
                case BUY -> update(BUY, asks, change.priceLevel(), change.size());
                case SELL -> update(SELL, bids, change.priceLevel(), change.size());
                default -> throw new IllegalArgumentException("Invalid side value " + change.size());
            }
        }
        return this;
    }

    private void update(Side side, double[][] priceLevels, BigDecimal priceLevel, BigDecimal size) {
        double level = priceLevel.doubleValue();
        if (side.equals(BUY) && level > asks[asks.length - 1][0])
            return;
        if (side.equals(SELL) && level < bids[bids.length - 1][0])
            return;

        var data = side.equals(BUY) ? asks : bids;
        var r = binarySearch(priceLevels, level);
        if (r >= 0) {
            priceLevels[r][1] += size.doubleValue();
        } else {
            double[] cur = new double[]{level, size.doubleValue()};
            double[] next = new double[2];

            if (side.equals(SELL)) {

                for (int i = 0; i < data.length; i++) {
                    if (data[i][1] < PIPS.doubleValue() && i != data.length - 1)
                        i++;
                    next[0] = data[i][0];
                    next[1] = data[i][1];

                    data[i][0] = cur[0];
                    data[i][1] = cur[1];

                    cur[0] = next[0];
                    cur[1] = next[1];
                }
            } else {
                for (int i = data.length - 1; i >= 0; i--) {
                    if (data[i][1] < PIPS.doubleValue() && i != 0)
                        i--;
                    next[0] = data[i][0];
                    next[1] = data[i][1];

                    data[i][0] = cur[0];
                    data[i][1] = cur[1];

                    cur[0] = next[0];
                    cur[1] = next[1];
                }
            }
        }

//        priceLevels();
        //FIXME temp patch
        double[][] collect = Arrays.stream(data)
                .filter(doubles -> doubles[1] < PIPS.doubleValue())
                .sorted(comparingDouble(doubles -> doubles[0]))
                .toList()
                .toArray(new double[0][]);
        for (int i = 0; i < collect.length; i++) {
            double[] datum = collect[i];
            data[i][0] = datum[0];
            data[i][1] = datum[1];
        }
        for (int i = collect.length; i < data.length; i++) {
            data[i][0] = 0;
            data[i][1] = 0;
        }
        if (side.equals(BUY)) {
            Arrays.sort(priceLevels, comparingDouble(d -> d[0]));
        } else {
            Arrays.sort(priceLevels, (d1, d2) -> Double.compare(d2[0], d1[0]));
        }
    }

    /**
     * for debug
     */
    private List<String> priceLevels() {
        List<String> res = new ArrayList<>(depth * 2 + 3);
        for (int i = asks.length - 1; i >= 0; i--) {
            double[] ask = asks[i];
            res.add(String.format("%8.2f %.8f", ask[0], ask[1]));
        }
        res.add(getQuote().toString());
        for (double[] bid : bids) {
            res.add(String.format("%8.2f %.8f", bid[0], bid[1]));
        }
        return res;
    }


    //boilerplate

    @Override
    public TwoWayQuote getQuote(BigDecimal volume) {
        return new TwoWayQuote(getPrice(bids, volume), getPrice(asks, volume));
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

    public Instant lastUpdate() {
        return lastUpdate;
    }

    public double[][] bids() {
        return bids;
    }

    public double[][] asks() {
        return asks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SingleVenueOrderBook) obj;
        return Objects.equals(this.productId, that.productId)
                && Objects.equals(this.venue, that.venue)
                && Objects.equals(this.lastUpdate, that.lastUpdate)
                && Objects.equals(this.bids, that.bids)
                && Objects.equals(this.asks, that.asks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(venue, productId, lastUpdate, bids, asks);
    }

    @Override
    public String toString() {
        return "OrderBook[" + "venue=" + venue + ", " + "productId=" + productId + ", "
                + "lastUpdate=" + lastUpdate + ", " + "bids=" + bids + ", " + "asks=" + asks + ']';
    }

}
