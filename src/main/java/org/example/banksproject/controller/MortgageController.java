package org.example.banksproject.controller;

import org.example.banksproject.models.ApiUser;
import org.example.banksproject.models.Bank;
import org.example.banksproject.models.Mortgage;
import org.example.banksproject.repositories.BankRepository;
import org.example.banksproject.repositories.MortgageRepository;
import org.example.banksproject.security.ApiKeyAuth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/banks/{bankId}/mortgages")
public class MortgageController {

    private final MortgageRepository mortgageRepository;
    private final BankRepository bankRepository;

    public MortgageController(MortgageRepository mortgageRepository, BankRepository bankRepository) {
        this.mortgageRepository = mortgageRepository;
        this.bankRepository = bankRepository;
    }

    /**
     *  Utility Methods
     */

    private ApiUser getCurrentPrincipal() {
        ApiKeyAuth auth = (ApiKeyAuth) SecurityContextHolder.getContext().getAuthentication();
        return auth.getPrincipal();
    }

    @GetMapping("/")
    public ResponseEntity<Iterable<Mortgage>> findAll(@PathVariable int bankId) {
        if (bankRepository.existsById(bankId)) {
            return ResponseEntity.ok(mortgageRepository.findByBankId(bankId));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{mortgageId}")
    public ResponseEntity<Mortgage> findById(@PathVariable int bankId, @PathVariable int mortgageId) {
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
        Optional<Bank> optionalBank = bankRepository.findById(bankId);
        if (optionalBank.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!optionalBank.get().isOwnedBy(getCurrentPrincipal().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Mortgage toSave = new Mortgage(null, bankId, body.getName(), body.getInterestRateType(), body.getDescription(), body.getApr());
        Mortgage savedMortgage = mortgageRepository.save(toSave);
        URI modelLocation = uriBuilder.path("/banks/{bankId}/mortgages/{id}")
                .buildAndExpand(bankId, savedMortgage.getId())
                .toUri();
        return ResponseEntity.created(modelLocation).body(savedMortgage);
    }

    @PutMapping("/{mortgageId}")
    public ResponseEntity<Mortgage> update(
            @PathVariable int bankId,
            @PathVariable int mortgageId,
            @RequestBody Mortgage mortgage)
    {
        Optional<Bank> optionalBank = bankRepository.findById(bankId);
        if (optionalBank.isEmpty() || !mortgageRepository.existsByIdAndBankId(mortgageId, bankId)) {
            return ResponseEntity.notFound().build();
        }

        if (!optionalBank.get().isOwnedBy(getCurrentPrincipal().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Mortgage toSave = new Mortgage(mortgageId, bankId, mortgage.getName(), mortgage.getInterestRateType(), mortgage.getDescription(), mortgage.getApr());
        Mortgage updatedMortgage = mortgageRepository.save(toSave);
        return ResponseEntity.ok(updatedMortgage);
    }

    @DeleteMapping("/{mortgageId}")
    public ResponseEntity<Void> delete(@PathVariable int bankId, @PathVariable int mortgageId) {
        Optional<Bank> optionalBank = bankRepository.findById(bankId);
        if (optionalBank.isEmpty() || !mortgageRepository.existsByIdAndBankId(mortgageId, bankId)) {
            return ResponseEntity.notFound().build();
        }

        if (!optionalBank.get().isOwnedBy(getCurrentPrincipal().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        mortgageRepository.deleteByIdAndBankId(mortgageId, bankId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{mortgageId}/simulate")
    public ResponseEntity<BigDecimal> simulateMortgage(
            @PathVariable int bankId,
            @PathVariable int mortgageId,
            @RequestParam double principal,
            @RequestParam int years)
    {
        Optional<Mortgage> optionalMortgage = mortgageRepository.findByIdAndBankId(mortgageId, bankId);
        if (optionalMortgage.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(calculateMonthlyMortgagePayment(principal, years, optionalMortgage.get()));
    }

    private BigDecimal calculateMonthlyMortgagePayment(double principal, int years, Mortgage mortgage) {
        double monthlyInterestRate = mortgage.getApr()/12;
        int months = years * 12;
        return BigDecimal.valueOf(principal *
                        (monthlyInterestRate * Math.pow(1 + monthlyInterestRate, months)) /
                        (Math.pow(1 + monthlyInterestRate, months) - 1))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
