package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.events.Event;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.ProductId;

import java.math.BigDecimal;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

public class DepthLimiterEventHandler implements OrderBookEventHandler {
    private final int defaultDepth;
    private final Map<ProductId, Integer> depthPerProduct;
    private final Map<ProductId, SortedSet<BigDecimal>> depth = new HashMap<>();

    public DepthLimiterEventHandler(int defaultDepth) {
        this(defaultDepth, new HashMap<>());
    }

    public DepthLimiterEventHandler(int defaultDepth, Map<ProductId, Integer> depthPerProduct) {
        this.defaultDepth = defaultDepth;
        this.depthPerProduct = depthPerProduct;
    }

    @Override
    public void onEvent(OrderBookUpdateEvent event, long sequence, boolean endOfBatch) {
        int productDepth = getDepth(event.productId());
        if (event.type().equals(Event.Type.SNAPSHOT)
                || event.type().equals(Event.Type.L2UPDATE)) {
            var changes = sortAndClean(event.changes(), productDepth * 2);
            depth.put(event.productId(), changes.stream()
                    .map(OrderBookUpdateEvent.PriceLevel::priceLevel)
                    .sorted()
                    .collect(toCollection(TreeSet::new)));
            event.setChanges(changes);
        }
    }

    private List<OrderBookUpdateEvent.PriceLevel> sortAndClean(List<OrderBookUpdateEvent.PriceLevel> changes, int productDepth) {
        var result = new ArrayList<OrderBookUpdateEvent.PriceLevel>();
        changes.stream()
                .collect(groupingBy(OrderBookUpdateEvent.PriceLevel::side, toCollection(TreeSet::new)))
                .forEach((side, priceLevels) -> {
                    var tmpres = new ArrayList<>(priceLevels);
                    switch (side) {
                        case BUY -> {
                            int buyIdx = Math.max(0, tmpres.size() - productDepth);
                            result.addAll(tmpres.subList(buyIdx, tmpres.size()));
                        }
                        case SELL -> {
                            int sellIdx = Math.min(tmpres.size(), productDepth);
                            result.addAll(tmpres.subList(0, sellIdx));
                        }
                    }
                });
        result.sort((pl1, pl2) -> pl2.priceLevel().compareTo(pl1.priceLevel()));
        return result;
    }

    public void setDepth(ProductId productId, int depth) {
        this.depthPerProduct.put(productId, depth);
    }

    public int getDepth(ProductId productId) {
        return depthPerProduct.getOrDefault(productId, defaultDepth);
    }
}
