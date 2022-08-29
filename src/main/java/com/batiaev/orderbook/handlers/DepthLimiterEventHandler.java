package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.events.Event;
import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.model.Side;

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
        var res = changes.stream()
                .collect(groupingBy(OrderBookUpdateEvent.PriceLevel::side, toCollection(TreeSet::new)));

        var buyres = new ArrayList<>(res.get(Side.BUY));
        var sellres = new ArrayList<>(res.get(Side.SELL));
        int buyIdx = Math.max(0, buyres.size() - productDepth);
        int sellIdx = Math.min(buyres.size(), productDepth);

        var result = new ArrayList<OrderBookUpdateEvent.PriceLevel>();
        result.addAll(buyres.subList(buyIdx, buyres.size()));
        result.addAll(sellres.subList(0, sellIdx));
        result.sort((pl1, pl2) -> pl2.priceLevel().compareTo(pl1.priceLevel()));
        return result;
    }

    public void setDepth(ProductId productId, int depth) {
        this.depthPerProduct.put(productId, depth);
    }

    public int getDepth(ProductId productId) {
        return depthPerProduct.getOrDefault(productId, defaultDepth);
    }

    private SortedSet<BigDecimal> newDepth() {
        return new TreeSet<>();
    }
}
