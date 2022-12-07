package com.batiaev.orderbook.providers;

import com.batiaev.orderbook.eventbus.EventEnricher;
import com.batiaev.orderbook.events.Event;
import com.batiaev.orderbook.events.OrderBookSubscribeEvent;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.handlers.LoggingEventHandler;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.TradingVenue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Cleaner;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static com.batiaev.orderbook.model.Side.BUY;
import static com.batiaev.orderbook.model.Side.SELL;
import static java.math.BigDecimal.valueOf;

public class RandomOrderBookProvider implements OrderBookProvider {
    private final Duration duration;
    private final Random random;
    private final ThreadLocal<AtomicLong> lastPrice = ThreadLocal.withInitial(() -> new AtomicLong(100));
    private static final Logger logger = LoggerFactory.getLogger(LoggingEventHandler.class);

    public RandomOrderBookProvider(Duration duration) {
        this.duration = duration;
        this.random = new Random();
    }

    @Override
    public OrderBookProvider start(OrderBookSubscribeEvent event, EventEnricher<OrderBookUpdateEvent> eventBus) {
        //        newSingleThreadScheduledExecutor().schedule(() -> eventBus.nextEvent(this::fillOrder), duration.toMillis(), TimeUnit.MILLISECONDS);
        Thread thread = new Thread(() -> {
            while (true) {
                eventBus.nextEvent(this::fillOrder);
                try {
                    Thread.sleep(duration.toMillis());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.setName("RANDOM_DATA_FEED");
        thread.start();
//        try {
//            newSingleThreadScheduledExecutor().schedule(() -> logger.error("TRIGGER"), duration.toMillis(), TimeUnit.MILLISECONDS).get();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }
        return this;
    }

    private OrderBookUpdateEvent fillOrder(OrderBookUpdateEvent orderBookUpdateEvent) {
        orderBookUpdateEvent.setProductId(ProductId.productId("ETH-USD"));
        var now = Instant.now();
        orderBookUpdateEvent.setTime(now);
        orderBookUpdateEvent.setType(Event.Type.L2UPDATE);
        orderBookUpdateEvent.setVenue(TradingVenue.RANDOM);
        int delta = (int) ((0.5 - random.nextDouble()) * 100);
        lastPrice.get().addAndGet(delta);
        BigDecimal size = valueOf(random.nextDouble());
        orderBookUpdateEvent.setChanges(List.of(new OrderBookUpdateEvent.PriceLevel(
                (random.nextInt() % 2) == 1 ? BUY : SELL, valueOf((lastPrice.get().get() / 100.)).add(size), size)));
        return orderBookUpdateEvent;
    }

    @Override
    public void sendMessage(OrderBookSubscribeEvent subscribeOn) {
        //do nothing
    }

    @Override
    public OrderBookProvider setStorage(Cleaner.Cleanable storage) {
        return this;
    }

    @Override
    public TradingVenue venueName() {
        return TradingVenue.RANDOM;
    }
}
