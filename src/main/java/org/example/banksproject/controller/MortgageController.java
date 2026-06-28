package org.example.banksproject.controller;

import org.example.banksproject.models.ApiUser;
import org.example.banksproject.models.Bank;
import org.example.banksproject.models.Mortgage;
import org.example.banksproject.repositories.BankRepository;
import org.example.banksproject.repositories.MortgageRepository;
import org.example.banksproject.security.ApiKeyAuth;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import org.example.banksproject.models.MortgageAttachment;
import org.example.banksproject.repositories.MortgageAttachmentRepository;
import org.example.banksproject.service.FileStorageService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/banks/{bankId}/mortgages")
public class MortgageController {

    private final MortgageRepository mortgageRepository;
    private final BankRepository bankRepository;
    private final MortgageAttachmentRepository mortgageAttachmentRepository;
    private final FileStorageService fileStorageService;

    public MortgageController(MortgageRepository mortgageRepository, BankRepository bankRepository, MortgageAttachmentRepository mortgageAttachmentRepository, FileStorageService fileStorageService) {
        this.mortgageRepository = mortgageRepository;
        this.bankRepository = bankRepository;
        this.mortgageAttachmentRepository = mortgageAttachmentRepository;
        this.fileStorageService = fileStorageService;
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

    @PostMapping("/{mortgageId}/attachments")
    public ResponseEntity<MortgageAttachment> uploadAttachment(
            @PathVariable int bankId,
            @PathVariable int mortgageId,
            @RequestParam("file") MultipartFile file,
            UriComponentsBuilder uriBuilder)
    {
        Optional<Bank> optionalBank = bankRepository.findById(bankId);
        if (optionalBank.isEmpty() || !mortgageRepository.existsByIdAndBankId(mortgageId, bankId)) {
            return ResponseEntity.notFound().build();
        }

        if (!optionalBank.get().isOwnedBy(getCurrentPrincipal().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            FileStorageService.StoredFile stored = fileStorageService.store(file, bankId, mortgageId);
            MortgageAttachment mortgageAttachmentToSave = new MortgageAttachment(null, mortgageId, stored.filename(), stored.contentType(), stored.size(), stored.path());
            MortgageAttachment saved = mortgageAttachmentRepository.save(mortgageAttachmentToSave);
            URI location = uriBuilder.path("/banks/{bankId}/mortgages/{mortgageId}/attachments/{id}")
                    .buildAndExpand(bankId, mortgageId, saved.getId())
                    .toUri();
            return ResponseEntity.created(location).body(saved);
        } catch (IllegalArgumentException | java.io.IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{mortgageId}/attachments")
    public ResponseEntity<Iterable<MortgageAttachment>> listAttachments(@PathVariable int bankId, @PathVariable int mortgageId) {
        if (!mortgageRepository.existsByIdAndBankId(mortgageId, bankId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mortgageAttachmentRepository.findByMortgageId(mortgageId));
    }

    @GetMapping("/{mortgageId}/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable int bankId, @PathVariable int mortgageId, @PathVariable int attachmentId) {
        Optional<MortgageAttachment> optionalAttachment = mortgageAttachmentRepository.findByIdAndMortgageId(attachmentId, mortgageId);
        if (!mortgageRepository.existsByIdAndBankId(mortgageId, bankId) || optionalAttachment.isEmpty()) return ResponseEntity.notFound().build();

        MortgageAttachment attachment = optionalAttachment.get();
        try {
            Resource resource = fileStorageService.loadAsResource(attachment.getPath());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFilename() + "\"")
                    .body(resource);
        } catch (java.io.IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{mortgageId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable int bankId, @PathVariable int mortgageId, @PathVariable int attachmentId) {
        Optional<Bank> optionalBank = bankRepository.findById(bankId);
        if (optionalBank.isEmpty() || !mortgageRepository.existsByIdAndBankId(mortgageId, bankId)) {
            return ResponseEntity.notFound().build();
        }

        if (!optionalBank.get().isOwnedBy(getCurrentPrincipal().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<MortgageAttachment> optionalAttachment = mortgageAttachmentRepository.findByIdAndMortgageId(attachmentId, mortgageId);
        if (optionalAttachment.isEmpty()) return ResponseEntity.notFound().build();

        MortgageAttachment attachment = optionalAttachment.get();
        mortgageAttachmentRepository.deleteByIdAndMortgageId(attachmentId, mortgageId);
        try {
            fileStorageService.delete(attachment.getPath());
        } catch (java.io.IOException e) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.noContent().build();
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
