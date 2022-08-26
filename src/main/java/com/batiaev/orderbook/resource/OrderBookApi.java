package com.batiaev.orderbook.resource;

import com.batiaev.orderbook.CoinbaseClient;
import com.batiaev.orderbook.handlers.OrderBookProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.batiaev.orderbook.events.OrderBookSubscribeEvent.subscribeOn;
import static com.batiaev.orderbook.model.ProductId.productId;
import static java.lang.Integer.parseInt;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;
import static spark.Spark.get;

public class OrderBookApi {
    private final CoinbaseClient client;
    private final String channel;
    private final OrderBookProcessor orderBookProcessor;

    public OrderBookApi(CoinbaseClient client, String channel, OrderBookProcessor orderBookProcessor) {
        this.client = client;
        this.channel = channel;
        this.orderBookProcessor = orderBookProcessor;
    }

    public void start() {
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
