package org.trading.messaging;

import com.google.protobuf.Timestamp;
import org.trading.MessageProvider.LimitOrderAccepted;
import org.trading.MessageProvider.Side;

public final class LimitOrderAcceptedBuilder {
    private Timestamp time = Timestamp.newBuilder().setSeconds(10000000L).build();
    private String id = "00000000-0000-0000-0000-000000000001";
    private String broker = "Broker";
    private int quantity = 1_000_000;
    private Side side = Side.BUY;
    private String symbol = "EURUSD";
    private double price = 1.47843;

    private LimitOrderAcceptedBuilder() {
    }

    public static LimitOrderAcceptedBuilder aLimitOrderAccepted() {
        return new LimitOrderAcceptedBuilder();
    }


    public LimitOrderAccepted build() {
        return LimitOrderAccepted.newBuilder()
                .setId(id)
                .setLimit(price)
                .setQuantity(quantity)
                .setTime(time)
                .setBroker(broker)
                .setSymbol(symbol)
                .setBroker(broker)
                .setSide(side)
                .build();
    }

    public LimitOrderAcceptedBuilder withTime(final Timestamp time) {
        this.time = time;
        return this;
    }

    public LimitOrderAcceptedBuilder withId(final String id) {
        this.id = id;
        return this;
    }

    public LimitOrderAcceptedBuilder withBroker(final String broker) {
        this.broker = broker;
        return this;
    }

    public LimitOrderAcceptedBuilder withQuantity(final int quantity) {
        this.quantity = quantity;
        return this;
    }

    public LimitOrderAcceptedBuilder withSide(final Side side) {
        this.side = side;
        return this;
    }

    public LimitOrderAcceptedBuilder withSymbol(final String symbol) {
        this.symbol = symbol;
        return this;
    }

    public LimitOrderAcceptedBuilder withPrice(final double price) {
        this.price = price;
        return this;
    }
}
