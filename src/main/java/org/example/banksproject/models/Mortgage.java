package org.example.banksproject.models;

import org.springframework.data.annotation.Id;

public class Mortgage {
    @Id private int id;
    private int bankId;
    private String name;
    private InterestRateType interestType;
    private String description;
    private double apr; // APR = TAE

    public Mortgage() {
    }

    public Mortgage(int id, int bankId, String name, InterestRateType interestType, String description, double apr) {
        this.id = id;
        this.bankId = bankId;
        this.name = name;
        this.interestType = interestType;
        this.description = description;
        this.apr = apr;
    }

    public static Mortgage copyMortgage(int mortgageId, Mortgage toCopy) {
        return new Mortgage(mortgageId, toCopy.bankId, toCopy.name, toCopy.interestType, toCopy.description, toCopy.apr);
    }

    public int getId() {return id;}
}
