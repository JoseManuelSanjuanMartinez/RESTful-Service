package org.example.banksproject.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;

public class Bank {

    @Id private int id;
    private String name;
    private String swiftBicCode;
    private String web;

    @PersistenceCreator
    public Bank(int id, String name, String swiftBicCode, String web) {
        this.id = id;
        this.name = name;
        this.swiftBicCode = swiftBicCode;
        this.web = web;
    }

    public Bank(int id, Bank toCopy) {
        this.id = toCopy.id;
        this.name = toCopy.name;
        this.swiftBicCode = toCopy.swiftBicCode;
        this.web = toCopy.web;
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
