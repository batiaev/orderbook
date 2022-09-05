package com.batiaev.orderbook.resource;

import com.batiaev.orderbook.events.OrderBookUpdateEvent;
import com.batiaev.orderbook.handlers.OrderBookHolder;
import com.batiaev.orderbook.model.ProductId;
import com.batiaev.orderbook.providers.CoinbaseClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import static com.batiaev.orderbook.events.OrderBookSubscribeEvent.withEvent;
import static com.batiaev.orderbook.model.ProductId.productId;
import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_PRECONDITION_FAILED;
import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;
import static spark.Service.SPARK_DEFAULT_PORT;
import static spark.Spark.*;

public class OrderBookApi {
    private final long MAX_WAIT = Duration.ofSeconds(30).toMillis();
    private static final Map<String, String> corsHeaders = Map.of(
            "Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS",
            "Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,",
            "Access-Control-Allow-Credentials", "true"
    );
    private final CoinbaseClient client;
    private final String channel;
    private final OrderBookHolder orderBookHolder;
    private final ObjectMapper objectMapper;

    public OrderBookApi(CoinbaseClient client, String channel,
                        OrderBookHolder orderBookHolder) {
        this(client, channel, orderBookHolder, new ObjectMapper());
    }

    public OrderBookApi(CoinbaseClient client, String channel,
                        OrderBookHolder orderBookHolder,
                        ObjectMapper objectMapper) {
        this.client = client;
        this.channel = channel;
        this.orderBookHolder = orderBookHolder;
        this.objectMapper = objectMapper;
    }

    public void start() {
        start(SPARK_DEFAULT_PORT);
    }

    public void start(int port) {
        Spark.port(port);
        after((request, response) -> corsHeaders.forEach(response::header));
        path("/orderbooks", () -> {
            get("", this::getOrderBooks);
            get("/:product", this::getOrderBook);
            get("/:product/groupBy/:group", this::groupOrderBook);
        });
    }

    private Object getOrderBooks(Request req, Response res) throws JsonProcessingException {
        return ok(res, orderBookHolder.orderBooksUpdates().entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private Object groupOrderBook(Request req, Response res) throws JsonProcessingException {
        var product = ofNullable(req.params("product")).map(ProductId::productId);
        if (product.isEmpty()) return res;
        var productId = product.get();
        var group = ofNullable(req.params("group")).map(BigDecimal::new).orElse(orderBookHolder.getGroup(productId));
        var ob = orderBookHolder.groupBy(productId, group);
        return ok(res, ob);
    }

    private Object getOrderBook(Request req, Response res) throws JsonProcessingException, InterruptedException {
        String prd = req.params("product");
        if (prd == null) return res;
        client.sendMessage(withEvent(channel, prd));
        var productId = productId(prd);
        var orderBook = orderBookHolder.orderBook(productId);
        long l = System.currentTimeMillis();
        while (orderBook == null) {
            if (System.currentTimeMillis() > l + MAX_WAIT)
                return error(res, SC_PRECONDITION_FAILED, "cannot connect to stream");
            orderBook = orderBookHolder.orderBook(productId);
            Thread.sleep(1000);
        }
        var queryParams = req.queryParams("depth");
        int v = queryParams != null ? parseInt(queryParams) : orderBook.getDepth();
        return ok(res, orderBook.orderBook(v).stream().map(OrderBookUpdateEvent.PriceLevel::toJson).toList());
    }

    private Object ok(Response res, Object result) throws JsonProcessingException {
        res.status(SC_OK);
        res.type(APPLICATION_JSON.toString());
        return objectMapper.writeValueAsString(result);
    }

    private Object error(Response res, int code, String errorMessage) {
        res.status(code);
        res.type(APPLICATION_JSON.toString());
        return errorMessage;
    }
}
