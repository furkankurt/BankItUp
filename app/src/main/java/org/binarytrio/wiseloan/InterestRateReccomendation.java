package org.binarytrio.wiseloan;

public enum InterestRateReccomendation {
    LOW_INTEREST(3.0),
    MEDIUM_INTEREST(5.0),
    HIGH_INTEREST(7.0)
    ;
    private Double value;

    InterestRateReccomendation(Double value) {
        this.value = value;
    }

    public Double getValue() {
        return value;
    }

    public static InterestRateReccomendation findByInterest(double interest) {
        for (InterestRateReccomendation rec : InterestRateReccomendation.values()) {
            if(rec.getValue().equals(interest)) {
                return rec;
            }
        }
        throw new IllegalArgumentException();
    }
}
