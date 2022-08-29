package com.batiaev.orderbook.resource;

import com.batiaev.orderbook.providers.CoinbaseClient;
import com.batiaev.orderbook.handlers.OrderBookHolder;
import com.batiaev.orderbook.model.ProductId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Request;
import spark.Response;

import java.math.BigDecimal;
import java.util.Map;

import static com.batiaev.orderbook.events.OrderBookSubscribeEvent.withEvent;
import static com.batiaev.orderbook.model.ProductId.productId;
import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;
import static spark.Spark.after;
import static spark.Spark.get;

public class OrderBookApi {
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
        after((request, response) -> corsHeaders.forEach(response::header));
        get("/orderBook/:product/groupBy/:group", this::groupOrderBook);
        get("/orderbook/:product", this::getOrderBook);
    }

    private Object groupOrderBook(Request req, Response res) throws JsonProcessingException {
        var product = ofNullable(req.params("product")).map(ProductId::productId);
        if (product.isEmpty()) return res;
        var productId = product.get();
        var group = ofNullable(req.params("group")).map(BigDecimal::new).orElse(orderBookHolder.getGroup(productId));
        var ob = orderBookHolder.groupBy(productId, group);
        return ok(res, ob);
    }

    private Object getOrderBook(Request req, Response res) throws JsonProcessingException {
        String prd = req.params("product");
        if (prd == null) return res;
        client.sendMessage(withEvent(channel, prd));
        var productId = productId(prd);
        var orderBook = orderBookHolder.orderBook(productId);
        while (orderBook == null) {
            orderBook = orderBookHolder.orderBook(productId);
        }
        var queryParams = req.queryParams("depth");
        int v = queryParams != null ? parseInt(queryParams) : orderBook.getDepth();
        return ok(res, orderBook.orderBook(v));
    }

    private Object ok(Response res, Object result) throws JsonProcessingException {
        res.status(SC_OK);
        res.type(APPLICATION_JSON.toString());
        return objectMapper.writeValueAsString(result);
    }
}
