package org.example.banksproject.models;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;

import java.util.Objects;

public class Mortgage {
    @Id private Integer id;
    private Integer bankId;
    private String name;
    private InterestRateType interestRateType;
    private String description;
    private double apr; // APR = TAE

    public Mortgage() {
    }

    public Mortgage(Integer id, Integer bankId, String name, InterestRateType interestRateType, String description, double apr) {
        this.id = id;
        this.bankId = bankId;
        this.name = name;
        this.interestRateType = interestRateType;
        this.description = description;
        this.apr = apr;
    }

    public static Mortgage copyMortgage(Integer mortgageId, Mortgage toCopy) {
        return new Mortgage(mortgageId, toCopy.bankId, toCopy.name, toCopy.interestRateType, toCopy.description, toCopy.apr);
    }

    public Integer getBankId() {return bankId;}

    public Integer getId() {return id;}

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Mortgage mortgage = (Mortgage) o;
        return Double.compare(getApr(), mortgage.getApr()) == 0 && Objects.equals(getId(), mortgage.getId()) && Objects.equals(getBankId(), mortgage.getBankId()) && Objects.equals(getName(), mortgage.getName()) && getInterestRateType() == mortgage.getInterestRateType() && Objects.equals(getDescription(), mortgage.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getBankId());
    }
}
