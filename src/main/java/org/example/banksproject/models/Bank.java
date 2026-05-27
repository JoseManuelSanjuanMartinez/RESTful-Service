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
}
