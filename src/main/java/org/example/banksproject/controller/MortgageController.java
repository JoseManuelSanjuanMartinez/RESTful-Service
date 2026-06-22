package org.example.banksproject.controller;

import org.example.banksproject.models.Mortgage;
import org.example.banksproject.repositories.BankRepository;
import org.example.banksproject.repositories.MortgageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/banks/{bankId}/mortgages")
class MortgageController {

    private final MortgageRepository mortgageRepository;
    private final BankRepository bankRepository;

    public MortgageController(MortgageRepository mortgageRepository, BankRepository bankRepository) {
        this.mortgageRepository = mortgageRepository;
        this.bankRepository = bankRepository;
    }

    @GetMapping("/")
    public ResponseEntity<Iterable<Mortgage>> findAll(@PathVariable Integer bankId) {
        if (bankRepository.existsById(bankId)) {
            return ResponseEntity.ok(mortgageRepository.findByBankId(bankId));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{mortgageId}")
    public ResponseEntity<Mortgage> findById(@PathVariable Integer bankId, @PathVariable Integer mortgageId) {
        Optional<Mortgage> optionalMortgage = mortgageRepository.findByIdAndBankId(mortgageId, bankId);
        if (optionalMortgage.isPresent()) {
            return ResponseEntity.ok(optionalMortgage.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<Mortgage> create(
            @PathVariable int bankId,
            @RequestBody Mortgage body,
            UriComponentsBuilder uriBuilder)
    {
        if (bankId == body.getBankId()) {
            Mortgage savedMortgage = mortgageRepository.save(body);
            URI modelLocation = uriBuilder.path("/banks/{bankId}/mortgages/{id}")
                    .buildAndExpand(bankId, body.getId())
                    .toUri();
            return ResponseEntity.created(modelLocation).body(savedMortgage);
        } else if (!bankRepository.existsById(bankId)) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{mortgageId}")
    public ResponseEntity<Mortgage> update(
            @PathVariable Integer bankId,
            @PathVariable Integer mortgageId,
            @RequestBody Mortgage mortgage)
    {
        Optional<Mortgage> optionalMortgage = mortgageRepository.findByIdAndBankId(mortgageId, bankId);
        if (optionalMortgage.isPresent()) {
            Mortgage updatedMortgage = mortgageRepository.save(Mortgage.copyMortgage(mortgageId, mortgage));
            return ResponseEntity.ok(updatedMortgage);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{mortgageId}")
    public ResponseEntity<?> delete(@PathVariable Integer bankId, @PathVariable Integer mortgageId) {
        Optional<Mortgage> optionalMortgage = mortgageRepository.findByIdAndBankId(mortgageId, bankId);
        if (optionalMortgage.isPresent()) {
            mortgageRepository.deleteByIdAndBankId(mortgageId, bankId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
