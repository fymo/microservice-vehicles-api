package com.udacity.vehicles.client.prices;

import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents the price of a given vehicle, including currency.
 */
@Embeddable
public class Price {

    @Transient
    private String currency;
    @Transient
    private BigDecimal price;
    @Transient
    private Long vehicleId;

    public Price() {
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price1 = (Price) o;
        return Objects.equals(currency, price1.currency) &&
                Objects.equals(price, price1.price) &&
                Objects.equals(vehicleId, price1.vehicleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, price, vehicleId);
    }
}
