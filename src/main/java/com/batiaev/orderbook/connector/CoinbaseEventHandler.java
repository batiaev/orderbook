package com.batiaev.orderbook.connector;

import com.batiaev.orderbook.events.ModelEvent;
import com.batiaev.orderbook.events.OrderBookSnapshotEvent;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.orderBook.OrderBook;
import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.batiaev.orderbook.model.orderBook.MapBasedOrderBook.orderBook;

public class CoinbaseEventHandler implements EventHandler<ModelEvent> {
    public static final Logger logger = LoggerFactory.getLogger(CoinbaseClient.class);
    private final int depth;
    private final Map<ProductId, OrderBook> orderBooks = new HashMap<>();
    private final Function<Long, Boolean> printerCondition;

    public CoinbaseEventHandler(int depth, Function<Long, Boolean> printerCondition) {
        this.depth = depth;
        this.printerCondition = printerCondition;
    }

    @Override
    public void onEvent(ModelEvent event, long sequence, boolean endOfBatch) {
        switch (event.type()) {
            case SNAPSHOT -> init((OrderBookSnapshotEvent) event.payload(), sequence);
            case L2UPDATE -> update((OrderBookUpdateEvent) event.payload(), sequence);
            default -> logger.warn("Unsupported event type " + event.type());
        }
    }

    private OrderBook init(OrderBookSnapshotEvent snapshot, long sequence) {
        logger.info("Processed snapshot seq=" + sequence);
        orderBooks.put(snapshot.productId(), orderBook(snapshot, depth));
        return orderBooks.get(snapshot.productId());
    }

    private void update(OrderBookUpdateEvent update, long sequence) {
        if (printerCondition.apply(sequence)) {
            for (OrderBook value : orderBooks.values()) {
                logger.info("{}", value);
            }
        }
        orderBooks.computeIfPresent(update.productId(), (productId, orderBook) -> orderBook.update(update));
    }
}
