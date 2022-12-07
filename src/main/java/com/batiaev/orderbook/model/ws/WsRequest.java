package com.batiaev.orderbook.model.ws;

import java.util.Objects;

public class WsRequest {
    private String type;
    private String product;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WsRequest wsRequest = (WsRequest) o;
        return Objects.equals(type, wsRequest.type) && Objects.equals(product, wsRequest.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, product);
    }

    @Override
    public String toString() {
        return "WsRequest{" +
                "type='" + type + '\'' +
                ", product='" + product + '\'' +
                '}';
    }
}
