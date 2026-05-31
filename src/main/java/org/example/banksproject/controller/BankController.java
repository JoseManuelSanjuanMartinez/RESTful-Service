package org.example.banksproject.controller;

import org.example.banksproject.models.Bank;
import org.example.banksproject.repositories.BankRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/banks")
class BankController {

    private final BankRepository bankRepository;

    BankController(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    @GetMapping("/")
    public ResponseEntity<Iterable<Bank>> findAll() {return ResponseEntity.ok(bankRepository.findAll());}

    @GetMapping("/{id}")
    public ResponseEntity<Bank> findById(@PathVariable int id) {
        Optional<Bank> optionalBank = bankRepository.findById(id);

        if (optionalBank.isPresent()) {
            return ok(optionalBank.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<Bank> create(@RequestBody Bank body, UriComponentsBuilder uriBuilder) {
        Bank bank = bankRepository.save(body);

        URI modelLocation = uriBuilder.path("/banks/{id}").buildAndExpand(bank.getId()).toUri();

        return ResponseEntity.ok(bank);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Bank body) {
        Optional<Bank> optionalBank = bankRepository.findById(id);
        if (optionalBank.isPresent()) {
            Bank updatedBank = bankRepository.save(new Bank(id, optionalBank.get()));
            return ResponseEntity.ok(updatedBank);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        Optional<Bank> optionalBank = bankRepository.findById(id);
        if (optionalBank.isPresent()) {
            bankRepository.delete(optionalBank.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
