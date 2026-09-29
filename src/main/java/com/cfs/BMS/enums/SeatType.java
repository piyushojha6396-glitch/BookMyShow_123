package com.cfs.BMS.enums;

import java.math.BigDecimal;

/**
 * Seat categories. The multiplier is applied on top of the show's base ticket price.
 */
public enum SeatType {
    REGULAR("1.00"),
    PREMIUM("1.50"),
    VIP("2.00");

    private final BigDecimal priceMultiplier;

    SeatType(String multiplier) {
        this.priceMultiplier = new BigDecimal(multiplier);
    }

    public BigDecimal getPriceMultiplier() {
        return priceMultiplier;
    }
}
