package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;

import java.math.BigDecimal;
import java.util.ArrayList;

import static java.util.Arrays.asList;

public class GroupingEventHandler implements OrderBookEventHandler {
    private BigDecimal group;

    public GroupingEventHandler(BigDecimal group) {
        this.group = group;
    }

    public BigDecimal getGroup() {
        return group;
    }

    public void setGroup(BigDecimal group) {
        this.group = group;
    }

    @Override
    public void onEvent(OrderBookUpdateEvent event, long sequence, boolean endOfBatch) {
        if (group.equals(BigDecimal.ZERO))
            return;
        ArrayList<OrderBookUpdateEvent.PriceLevel> levels = new ArrayList<>(event.changes());
        OrderBookUpdateEvent.PriceLevel[] res = new OrderBookUpdateEvent.PriceLevel[levels.size()];
        int idx = 0;
        for (OrderBookUpdateEvent.PriceLevel change : event.changes()) {
            res[idx++] = change.round(group);
        }
        event.setChanges(asList(res));
    }
}
