package com.batiaev.orderbook.eventbus;

import java.util.concurrent.ThreadFactory;

public class CoreTreadFactory implements ThreadFactory {
    public static final int idx = 0;

    public final static ThreadFactory CEQ = new CoreTreadFactory();

    @Override
    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setName("CORE-EVENT-QUEUE-" + idx);
        t.setDaemon(true);
        return t;
    }
}
