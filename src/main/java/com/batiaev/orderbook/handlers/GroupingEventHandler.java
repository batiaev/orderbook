package com.batiaev.orderbook.handlers;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;

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
        if (group.equals(ZERO))
            return;
        for (OrderBookUpdateEvent.PriceLevel change : event.changes()) {
            change.round(group);
        }
    }
}
