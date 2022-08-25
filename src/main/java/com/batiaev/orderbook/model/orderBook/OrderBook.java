package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.events.OrderBookSnapshotEvent;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.events.PriceLevel;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.TradingVenue;
import com.batiaev.orderbook.model.TwoWayQuote;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.Side.SELL;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;

public interface OrderBook {
    BigDecimal PIPS = BigDecimal.valueOf(0.00000001);

    TradingVenue getVenue();

    ProductId getProductId();

    int getDepth();

    /**
     * @return best bid offer
     */
    default TwoWayQuote getQuote() {
        return getQuote(ZERO);
    }

    /**
     * @param volume - required trade volume
     * @return vwap for requested volume
     */
    TwoWayQuote getQuote(BigDecimal volume);

    /**
     * @return resized order book to new depth, if new depth more that current - order book will have zero at the end of order book
     */
//    OrderBook resize(int depth);

    /**
     * @return new order book with grouped price levels according to provided steps
     */
//    OrderBook group(int step);

    /**
     * delete internal state and reinitialise with new snapshot
     * Note: will be used same depth as for original order book
     */
//    OrderBook reset(OrderBookSnapshotEvent event);

    /**
     * remove all price levels of order book
     */
//    default OrderBook reset() {
//        return reset(emtpySnapshot(getProductId()));
//    }

    /**
     * reset state of order book to snapshot with resize to new depth
     * Note: if new depth more that current - order book will contain zero price level at the end
     */
//    OrderBook reset(OrderBookSnapshotEvent event, int depth);

    /**
     * apply changes to existing order book
     */
    OrderBook update(ProductId productId, Instant time, List<PriceLevel> changes);

    default OrderBook update(OrderBookSnapshotEvent event) {
        List<PriceLevel> changes = new ArrayList<>();
        for (double[] ask : event.asks()) {
            changes.add(new PriceLevel(BUY, valueOf(ask[0]), valueOf(ask[1])));
        }
        for (double[] bid : event.bids()) {
            changes.add(new PriceLevel(SELL, valueOf(bid[0]), valueOf(bid[1])));
        }
        return update(event.productId(), Instant.EPOCH, changes);
    }

    default OrderBook update(OrderBookUpdateEvent event) {
        return update(event.productId(), event.time(), event.changes());
    }
}
