package org.example.banksproject.models;

import org.springframework.data.annotation.Id;

public class Bank {

    @Id private int id;
    private String name;
    private String swiftBicCode;
    private String web;

    public Bank(){}

    public Bank(int id, String name, String swiftBicCode, String web) {
        this.id = id;
        this.name = name;
        this.swiftBicCode = swiftBicCode;
        this.web = web;
    }

    public static Bank copyBank(int id, Bank toCopy) {
        return new Bank(id, toCopy.name, toCopy.swiftBicCode, toCopy.web);
    }

    /**
     * GETTERS
     */

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSwiftBicCode() {
        return swiftBicCode;
    }

    public String getWeb() {
        return web;
    }
}
