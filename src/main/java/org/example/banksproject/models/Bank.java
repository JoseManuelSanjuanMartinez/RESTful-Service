package org.example.banksproject.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Bank {

    @Id
    @GeneratedValue
    private int id;
    private String name;
    private String swiftBicCode;
    private String web;

    public Bank() {
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
}
