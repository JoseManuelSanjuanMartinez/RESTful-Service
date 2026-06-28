package org.example.banksproject.repositories;

import org.example.banksproject.models.PrivateMortgageAttachment;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MortgageAttachmentRepository extends CrudRepository<PrivateMortgageAttachment, Integer> {

    Iterable<PrivateMortgageAttachment> findByMortgageId(Integer mortgageId);

    Optional<PrivateMortgageAttachment> findByIdAndMortgageId(Integer id, Integer mortgageId);

    boolean existsByIdAndMortgageId(Integer id, Integer mortgageId);

    void deleteByIdAndMortgageId(Integer id, Integer mortgageId);
}

