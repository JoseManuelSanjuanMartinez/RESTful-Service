package org.example.banksproject.models;

import org.springframework.data.annotation.Id;

public class Mortgage {
    @Id
    private int id;
    private int bankId;
    private String name;
    private InterestRateType interestType;
    private String description;
    private double apr; // APR = TAE


}
