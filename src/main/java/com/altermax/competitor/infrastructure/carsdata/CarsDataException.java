package com.altermax.competitor.infrastructure.carsdata;

public class CarsDataException extends RuntimeException {
    public enum Kind {
        NOT_FOUND,
        RATE_LIMIT,
        TIMEOUT,
        UNAVAILABLE,
        INVALID_RESPONSE
    }

    private final Kind kind;

    public CarsDataException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }
}
