package org.example.banksproject.controller;

import org.example.banksproject.models.ApiUser;
import org.example.banksproject.models.Bank;
import org.example.banksproject.repositories.BankRepository;
import org.example.banksproject.security.ApiKeyAuth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

    /**
     *  Utility Methods
     */

    private ApiUser getCurrentPrincipal() {
        ApiKeyAuth auth = (ApiKeyAuth) SecurityContextHolder.getContext().getAuthentication();
        return auth.getPrincipal();
    }

    /**
     * Request Mappings
     */

    @GetMapping("/")
    public ResponseEntity<Iterable<Bank>> findAll() {return ResponseEntity.ok(bankRepository.findAll());}

    @GetMapping("/{bankId}")
    public ResponseEntity<Bank> findById(@PathVariable Integer bankId) {
        Optional<Bank> optionalBank = bankRepository.findById(bankId);

        if (optionalBank.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        return ok(optionalBank.get());
    }

    @PostMapping("/")
    public ResponseEntity<Bank> create(@RequestBody Bank body, UriComponentsBuilder uriBuilder) {
        ApiUser currentUser = getCurrentPrincipal();
        body.setOwnerId(currentUser.getId());

        Bank bank = bankRepository.save(body);

        URI modelLocation = uriBuilder.path("/banks/{id}").buildAndExpand(bank.getId()).toUri();

        return ResponseEntity.created(modelLocation).body(bank);
    }

    @PutMapping("/{bankId}")
    public ResponseEntity<Bank> update(@PathVariable Integer bankId, @RequestBody Bank body) {
        if (!bankRepository.existsById(bankId)) {
            return ResponseEntity.notFound().build();
        }

        if (!body.isOwnedBy(getCurrentPrincipal().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Bank updatedBank = bankRepository.save(Bank.copyBank(bankId, body));

        return ResponseEntity.ok(updatedBank);
    }

    @DeleteMapping("/{bankId}")
    public ResponseEntity<Void> delete(@PathVariable Integer bankId) {
        Optional<Bank> optionalBank = bankRepository.findById(bankId);
        if (optionalBank.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!optionalBank.get().isOwnedBy(getCurrentPrincipal().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        bankRepository.delete(optionalBank.get());
        return ResponseEntity.noContent().build();
    }
}
