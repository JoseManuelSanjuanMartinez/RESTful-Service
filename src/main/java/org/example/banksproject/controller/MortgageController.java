package org.example.banksproject.controller;

import org.example.banksproject.models.Mortgage;
import org.example.banksproject.repositories.BankRepository;
import org.example.banksproject.repositories.MortgagesRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/banks/{bankId}/mortgages")
class MortgageController {

    private final MortgagesRepository mortgagesRepository;
    private final BankRepository bankRepository;

    public MortgageController(MortgagesRepository mortgagesRepository, BankRepository bankRepository) {
        this.mortgagesRepository = mortgagesRepository;
        this.bankRepository = bankRepository;
    }

    @GetMapping("/")
    public ResponseEntity<Iterable<Mortgage>> findAll(@PathVariable Integer bankId) {
        //TODO
        return null;
    }

    @GetMapping("/{mortgageId}")
    public ResponseEntity<Mortgage> findById(@PathVariable Integer bankId, @PathVariable Integer mortgageId) {
        //TODO
        return null;
    }

    @PostMapping("/")
    public ResponseEntity<Mortgage> create(@PathVariable Integer bankId, @RequestBody Mortgage mortgage) {
        //TODO
        return null;
    }

    @PutMapping("/{mortgageId}")
    public ResponseEntity<Mortgage> update(@PathVariable Integer bankId, @PathVariable Integer mortgageId) {
        //TODO
        return null;
    }

    @DeleteMapping("/{mortgageId}")
    public ResponseEntity<?> delete(@PathVariable Integer bankId, @PathVariable Integer mortgageId) {
        //TODO
        return null;
    }
}
