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
public class BankController {

    private final BankRepository bankRepository;

    public BankController(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    @GetMapping("/")
    public ResponseEntity<Iterable<Bank>> findAll() {return ResponseEntity.ok(bankRepository.findAll());}

    @GetMapping("/{bankId}")
    public ResponseEntity<Bank> findById(@PathVariable Integer bankId) {
        Optional<Bank> optionalBank = bankRepository.findById(bankId);

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

        return ResponseEntity.created(modelLocation).body(bank);
    }

    @PutMapping("/{bankId}")
    public ResponseEntity<Bank> update(@PathVariable Integer bankId, @RequestBody Bank body) {
        if (bankRepository.existsById(bankId)) {
            Bank updatedBank = bankRepository.save(Bank.copyBank(bankId, body));
            return ResponseEntity.ok(updatedBank);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{bankId}")
    public ResponseEntity<Void> delete(@PathVariable Integer bankId) {
        Optional<Bank> optionalBank = bankRepository.findById(bankId);
        if (optionalBank.isPresent()) {
            bankRepository.delete(optionalBank.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
