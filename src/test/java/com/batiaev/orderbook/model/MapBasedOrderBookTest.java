package com.batiaev.orderbook.model;

import com.batiaev.orderbook.events.Event;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.orderBook.MapBasedOrderBook;
import com.batiaev.orderbook.model.orderBook.OrderBook;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.batiaev.orderbook.events.OrderBookUpdateEvent.PriceLevel.priceLevel;
import static com.batiaev.orderbook.model.ProductId.productId;
import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.Side.SELL;
import static com.batiaev.orderbook.model.TradingVenue.COINBASE;
import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MapBasedOrderBookTest extends OrderBookTest {

    @Test
    void should_return_orderBook_with_smaller_depth() {
        //given
        var orderBook = new MapBasedOrderBook(COINBASE, productId("ETH-USD"), now(), 3,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var expected = List.of(
                priceLevel(SELL, 60, 10),
                priceLevel(SELL, 50, 10),
                priceLevel(BUY, 30, 10),
                priceLevel(BUY, 20, 10)
        );
        //when
        var priceLevels = orderBook.orderBook(2);
        //then
        assertEquals(expected, priceLevels);
    }

    @Test
    void should_return_orderBook_with_larger_depth() {
        //given
        var orderBook = new MapBasedOrderBook(COINBASE, productId("ETH-USD"), now(), 3,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var expected = List.of(
                priceLevel(SELL, 70, 10),
                priceLevel(SELL, 60, 10),
                priceLevel(SELL, 50, 10),
                priceLevel(BUY, 30, 10),
                priceLevel(BUY, 20, 10),
                priceLevel(BUY, 10, 10)
        );
        //when
        var priceLevels = orderBook.orderBook(100);
        //then
        assertEquals(expected, priceLevels);
    }

    /**
     * SIDE   S  S  S X   B   B  B  B
     * PRICE 70 60 50 X (40) 30 20 10
     * SIZE  10 10 10 X (+5) 10 10 10
     */
    @Test
    void should_add_new_best_buy_order() {
        var productId = productId("ETH-USD");
        var depth = 3;
        var orderBook = new MapBasedOrderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var expected = new MapBasedOrderBook(COINBASE, productId, now(), depth,
                orderBook(40, 5, 30, 10, 20, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var event = new OrderBookUpdateEvent(Event.Type.L2UPDATE, COINBASE, productId, now(),
                List.of(priceLevel(BUY, 40, 5)));
        //when
        orderBook.update(event);
        //then
        assertEquals(expected.orderBook(depth), orderBook.orderBook(depth));
    }

    /**
     * SIDE   S  S  S X   B   B  B  B
     * PRICE 70 60 50 X (40) 30 20 10
     * SIZE  10 10 10 X (+5) 10 10 10
     */
    @Test
    void should_add_new_mid_buy_order() {
        var productId = productId("ETH-USD");
        var depth = 3;
        var orderBook = new MapBasedOrderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var expected = new MapBasedOrderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 25, 5, 20, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var event = new OrderBookUpdateEvent(Event.Type.L2UPDATE, COINBASE, productId, now(),
                List.of(priceLevel(BUY, 25, 5)));
        //when
        orderBook.update(event);
        //then
        assertEquals(expected.orderBook(depth), orderBook.orderBook(depth));
    }

    /**
     * SIDE   S  S  S   S  X  B  B  B
     * PRICE 70 60 50 (40) X 30 20 10
     * SIZE  10 10 10 (+5) X 10 10 10
     */
    @Test
    void should_add_new_best_sell_order() {
        var productId = productId("ETH-USD");
        var depth = 3;
        var orderBook = new MapBasedOrderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var expected = new MapBasedOrderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(40, 5, 50, 10, 60, 10));
        var event = new OrderBookUpdateEvent(Event.Type.L2UPDATE, COINBASE, productId, now(),
                List.of(priceLevel(SELL, 40, 5)));
        //when
        orderBook.update(event);

        System.out.println(orderBook.orderBook(depth));
        //then
        assertEquals(expected.orderBook(depth), orderBook.orderBook(depth));
    }

    /**
     * SIDE   S  S   S   S X  B  B  B
     * PRICE 70 60 (55) 50 X 30 20 10
     * SIZE  10 10 (+5) 10 X 10 10 10
     */
    @Test
    void should_add_new_mid_sell_order() {
        var productId = productId("ETH-USD");
        var depth = 3;
        var orderBook = new MapBasedOrderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var expected = new MapBasedOrderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 55, 5, 60, 10));
        var event = new OrderBookUpdateEvent(Event.Type.L2UPDATE, COINBASE, productId, now(),
                List.of(priceLevel(SELL, 55, 5)));
        //when
        orderBook.update(event);

        System.out.println(orderBook.orderBook(depth));
        //then
        assertEquals(expected.orderBook(depth), orderBook.orderBook(depth));
    }

    /**
     * SIDE   S  S  S X  B  B  B
     * PRICE 70 60 50 X 30 20 10
     * SIZE  10 10 10 X  5 10 10
     */
    @Test
    void should_update_buy_order() {
        var productId = productId("ETH-USD");
        var depth = 3;
        var orderBook = new MapBasedOrderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var expected = new MapBasedOrderBook(COINBASE, productId, now(), depth,
                orderBook(30, 5, 20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var event = new OrderBookUpdateEvent(Event.Type.L2UPDATE, COINBASE, productId, now(),
                List.of(priceLevel(BUY, 30, 5)));
        //when
        orderBook.update(event);
        //then
        assertEquals(expected.orderBook(depth), orderBook.orderBook(depth));
    }

    /**
     * SIDE   S  S  S X  B  B  B
     * PRICE 70 60 50 X 30 20 10
     * SIZE  10  5 10 X 10 10 10
     */
    @Test
    void should_update_sell_order() {
        var productId = productId("ETH-USD");
        var depth = 3;
        var orderBook = new MapBasedOrderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var expected = new MapBasedOrderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 60, 5, 70, 10));
        var event = new OrderBookUpdateEvent(Event.Type.L2UPDATE, COINBASE, productId, now(),
                List.of(priceLevel(SELL, 60, 5)));
        //when
        orderBook.update(event);

        System.out.println(orderBook.orderBook(depth));
        //then
        assertEquals(expected.orderBook(depth), orderBook.orderBook(depth));
    }

    /**
     * SIDE   S  S  S X  B  B
     * PRICE 70 60 50 X 20 10
     * SIZE  10 10 10 X 10 10
     */
    @Test
    void should_delete_best_buy_order() {
        var productId = productId("ETH-USD");
        var depth = 3;
        var orderBook = orderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var expected = orderBook(COINBASE, productId, now(), depth,
                orderBook(20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var event = new OrderBookUpdateEvent(Event.Type.L2UPDATE, COINBASE, productId, now(),
                List.of(priceLevel(BUY, 30, 0)));
        //when
        orderBook.update(event);
        //then
        List<OrderBookUpdateEvent.PriceLevel> actual = orderBook.orderBook(depth);
        assertEquals(expected.orderBook(depth), actual);
    }

    /**
     * SIDE   S  S X  B  B  B
     * PRICE 70 60 X 30 20 10
     * SIZE  10 10 X 10 10 10
     */
    @Test
    void should_delete_best_sell_order() {
        var productId = productId("ETH-USD");
        var depth = 3;
        var orderBook = orderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var expected = orderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(60, 10, 70, 10));
        var event = new OrderBookUpdateEvent(Event.Type.L2UPDATE, COINBASE, productId, now(),
                List.of(priceLevel(SELL, 50, 0)));
        //when
        orderBook.update(event);

        System.out.println(orderBook.orderBook(depth));
        //then
        assertEquals(expected.orderBook(depth), orderBook.orderBook(depth));
    }

    @Test
    void should_override_best_sell_with_new_buy_order() {
        var productId = productId("ETH-USD");
        var depth = 3;
        var orderBook = orderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var expected = orderBook(COINBASE, productId, now(), depth,
                orderBook(50, 5, 30, 10, 20, 10),
                orderBook(60, 10, 70, 10));
        var event = new OrderBookUpdateEvent(Event.Type.L2UPDATE, COINBASE, productId, now(),
                List.of(priceLevel(BUY, 50, 5)));
        //when
        orderBook.update(event);
        //then
        List<OrderBookUpdateEvent.PriceLevel> actual = orderBook.orderBook(depth);
        assertEquals(expected.orderBook(depth), actual);
    }

    @Test
    void should_override_best_buy_with_new_sell_order() {
        var productId = productId("ETH-USD");
        var depth = 3;
        var orderBook = orderBook(COINBASE, productId, now(), depth,
                orderBook(30, 10, 20, 10, 10, 10),
                orderBook(50, 10, 60, 10, 70, 10));
        var expected = orderBook(COINBASE, productId, now(), depth,
                orderBook(20, 10, 10, 10),
                orderBook(30, 5, 50, 10, 60, 10));
        var event = new OrderBookUpdateEvent(Event.Type.L2UPDATE, COINBASE, productId, now(),
                List.of(priceLevel(SELL, 30, 5)));
        //when
        orderBook.update(event);

        System.out.println(orderBook.orderBook(depth));
        //then
        assertEquals(expected.orderBook(depth), orderBook.orderBook(depth));
    }

    @Override
    OrderBook orderBook(TradingVenue venue, ProductId productId, Instant lastUpdate, int depth,
                        Map<BigDecimal, BigDecimal> bids, Map<BigDecimal, BigDecimal> asks) {
        return new MapBasedOrderBook(venue, productId, lastUpdate, depth, bids, asks);
    }
}
