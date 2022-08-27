package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.TwoWayQuote;
import com.batiaev.orderbook.model.orderBook.OrderBook;
import com.batiaev.orderbook.model.orderBook.OrderBookFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.batiaev.orderbook.events.Event.Type.SNAPSHOT;
import static com.batiaev.orderbook.events.OrderBookUpdateEvent.PriceLevel.priceLevel;
import static com.batiaev.orderbook.model.ProductId.productId;
import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.Side.SELL;
import static com.batiaev.orderbook.model.TradingVenue.COINBASE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.valueOf;
import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderBookProcessorTest {
    @Test
    void should_create_order_book() {
        //given
        int depth = 5;
        var processor = new OrderBookProcessor(new OrderBookFactory(OrderBook.Type.TREE_MAP), depth);
        var productId = productId("ETH-USD");
        var event = new OrderBookUpdateEvent(SNAPSHOT, COINBASE, productId, now(),
                List.of(priceLevel(BUY, 10, 10), priceLevel(SELL, 20, 10)));
        //when
        processor.onEvent(event, 1, true);
        //then
        var orderBook = processor.orderBook(productId);
        assertNotNull(orderBook);
        assertEquals(productId, orderBook.getProductId());
        assertEquals(depth, orderBook.getDepth());
        assertEquals(new TwoWayQuote(TEN, valueOf(20)), orderBook.getQuote());
        assertEquals(COINBASE, orderBook.getVenue());
    }
}
