package com.batiaev.orderbook.resource;

import com.batiaev.orderbook.CoinbaseClient;
import com.batiaev.orderbook.handlers.GroupingEventHandler;
import com.batiaev.orderbook.handlers.OrderBookProcessor;
import com.batiaev.orderbook.model.ProductId;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Map;

import static com.batiaev.orderbook.events.OrderBookSubscribeEvent.subscribeOn;
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
    private final OrderBookProcessor orderBookProcessor;
    private final GroupingEventHandler groupingEventHandler;

    public OrderBookApi(CoinbaseClient client, String channel,
                        OrderBookProcessor orderBookProcessor,
                        GroupingEventHandler groupingEventHandler) {
        this.client = client;
        this.channel = channel;
        this.orderBookProcessor = orderBookProcessor;
        this.groupingEventHandler = groupingEventHandler;
    }

    public void start() {
        after((request, response) -> corsHeaders.forEach(response::header));
        get("/orderBook/:product/groupBy/:group", (req, res) -> {
            var product = ofNullable(req.params("product")).map(ProductId::productId);
            if (product.isEmpty()) return res;
            var productId = product.get();
            var group = ofNullable(req.params("group")).map(BigDecimal::new).orElse(groupingEventHandler.getGroup());
            var ob = orderBookProcessor.groupBy(productId, group);
            groupingEventHandler.setGroup(group);
            res.status(SC_OK);
            res.type(APPLICATION_JSON.toString());
            return ob;
        });
        get("/orderbook/:product", (req, res) -> {
            String prd = req.params("product");
            if (prd != null) {
                client.sendMessage(subscribeOn(channel, prd));
            }
            var productId = productId(prd);
            var orderBook = orderBookProcessor.orderBook(productId);
            while (orderBook == null) {
                orderBook = orderBookProcessor.orderBook(productId);
            }
            var queryParams = req.queryParams("depth");
            int v = queryParams != null ? parseInt(queryParams) : orderBook.getDepth();
            res.status(SC_OK);
            res.type(APPLICATION_JSON.toString());
            return new ObjectMapper().writeValueAsString(orderBook.orderBook(v));
        });
    }
}
