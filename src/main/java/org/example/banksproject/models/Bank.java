package org.example.banksproject.models;

import org.springframework.data.annotation.Id;

public class Bank {

    @Id private Integer id;
    private String name;
    private String swiftBicCode;
    private String web;
    private Integer ownerId;

    public Bank(){}

    public Bank(Integer id, String name, String swiftBicCode, String web, Integer ownerId) {
        this.id = id;
        this.name = name;
        this.swiftBicCode = swiftBicCode;
        this.web = web;
        this.ownerId = ownerId;
    }

    public static Bank copyBank(Integer id, Bank toCopy) {
        return new Bank(
                id,
                toCopy.name,
                toCopy.swiftBicCode,
                toCopy.web,
                toCopy.ownerId
        );
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

    public boolean isOwnedBy(Integer ownerId) {
        return ownerId.equals(this.ownerId);
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }
}
