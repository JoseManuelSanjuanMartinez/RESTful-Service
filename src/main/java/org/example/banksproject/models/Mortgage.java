package org.example.banksproject.models;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;

public class Mortgage {
    @Id private int id;
    private int bankId;
    private String name;
    private InterestRateType interestRateType;
    private String description;
    private double apr; // APR = TAE

    public Mortgage() {
    }

    public Mortgage(int id, int bankId, String name, InterestRateType interestRateType, String description, double apr) {
        this.id = id;
        this.bankId = bankId;
        this.name = name;
        this.interestRateType = interestRateType;
        this.description = description;
        this.apr = apr;
    }

    public static Mortgage copyMortgage(int mortgageId, Mortgage toCopy) {
        return new Mortgage(mortgageId, toCopy.bankId, toCopy.name, toCopy.interestRateType, toCopy.description, toCopy.apr);
    }

    public int getBankId() {return bankId;}

    public int getId() {return id;}

    public String getName() {
        return name;
    }

    public InterestRateType getInterestRateType() {
        return interestRateType;
    }

    public String getDescription() {
        return description;
    }

    public double getApr() {
        return apr;
    }
}
