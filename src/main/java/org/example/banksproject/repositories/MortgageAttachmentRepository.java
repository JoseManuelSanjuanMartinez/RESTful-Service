package org.example.banksproject.repositories;

import org.example.banksproject.models.MortgageAttachment;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MortgageAttachmentRepository extends CrudRepository<MortgageAttachment, Integer> {

    Iterable<MortgageAttachment> findByMortgageId(Integer mortgageId);

    Optional<MortgageAttachment> findByIdAndMortgageId(Integer id, Integer mortgageId);

    boolean existsByIdAndMortgageId(Integer id, Integer mortgageId);

    void deleteByIdAndMortgageId(Integer id, Integer mortgageId);
}

