package org.trading.api;

import org.trading.api.event.LimitOrderAccepted;
import org.trading.api.message.Side;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;

public final class LimitOrderAcceptedBuilder {
    private LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);
    private UUID id = new UUID(0, 1);
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
        return new LimitOrderAccepted(
                id,
                time,
                broker,
                quantity,
                side,
                price,
                symbol
        );
    }

    public LimitOrderAcceptedBuilder withTime(final LocalDateTime time) {
        this.time = time;
        return this;
    }

    public LimitOrderAcceptedBuilder withId(final UUID id) {
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
