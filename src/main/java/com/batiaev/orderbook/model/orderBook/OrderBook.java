package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.TradingVenue;
import com.batiaev.orderbook.model.TwoWayQuote;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static java.math.BigDecimal.ZERO;

public interface OrderBook {
    enum Type {
        MAP_BASED,
        LONG_ARRAY
    }

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
    OrderBook update(ProductId productId, Instant time, List<OrderBookUpdateEvent.PriceLevel> changes);

    default OrderBook update(OrderBookUpdateEvent event) {
        return update(event.productId(), event.time(), event.changes());
    }

    default List<OrderBookUpdateEvent.PriceLevel> orderBook() {
        return orderBook(getDepth());
    }

    List<OrderBookUpdateEvent.PriceLevel> orderBook(int depth);
}
