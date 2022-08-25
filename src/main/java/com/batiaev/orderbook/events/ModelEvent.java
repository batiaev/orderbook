package com.batiaev.orderbook.events;

import java.util.Objects;

public final class ModelEvent<E extends Event> implements Event {
    private Type type = Type.UNKNOWN;
    private E payload;

    public ModelEvent() {
    }

    public ModelEvent(Type type, E payload) {
        this.type = type;
        this.payload = payload;
    }

    public Type type() {
        return type;
    }

    public E payload() {
        return payload;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setPayload(E payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ModelEvent) obj;
        return Objects.equals(this.type, that.type) && Objects.equals(this.payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, payload);
    }

    @Override
    public String toString() {
        return "ModelEvent[" + "type=" + type + ", " + "payload=" + payload + ']';
    }
}
