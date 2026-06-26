package org.example.banksproject.models;

import org.springframework.data.annotation.Id;

public class Bank {

    @Id private Integer id;
    private String name;
    private String swiftBicCode;
    private String web;

    public Bank(){}

    public Bank(Integer id, String name, String swiftBicCode, String web) {
        this.id = id;
        this.name = name;
        this.swiftBicCode = swiftBicCode;
        this.web = web;
    }

    public static Bank copyBank(Integer id, Bank toCopy) {
        return new Bank(id, toCopy.name, toCopy.swiftBicCode, toCopy.web);
    }

    /**
     * GETTERS
     */

    public Integer getId() {
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
