package com.batiaev.orderbook;

import com.batiaev.orderbook.model.orderBook.OrderBook;
import com.batiaev.orderbook.serializer.OrderBookEventParser;
import com.neovisionaries.ws.client.WebSocketException;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.batiaev.orderbook.events.OrderBookSubscribeEvent.withEvent;
import static com.batiaev.orderbook.model.ProductId.productId;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 5, timeUnit = TimeUnit.MILLISECONDS, time = 5000)
@Measurement(iterations = 5, timeUnit = TimeUnit.MILLISECONDS, time = 5000)
public class OrderBookJmhTest {

    @Benchmark
    public void eventProcessing(Blackhole bh) throws WebSocketException, IOException {
        var product = productId("ETH-USD");
        int depth = 10;
        var channel = "level2";
        var type = OrderBook.Type.LONG_MAP;
        var coinbaseClient = new CoinbaseClient(new OrderBookEventParser());

        var orderBook = OrderBook.basedOn(type)
                .withGroupingBy(0.01)
                .withDepth(depth)
                .withLoggingFrequency(0)
                .subscribedOn(coinbaseClient)
                .start(withEvent(channel, product));
        bh.consume(orderBook.orderBook(product));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(OrderBookJmhTest.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}

