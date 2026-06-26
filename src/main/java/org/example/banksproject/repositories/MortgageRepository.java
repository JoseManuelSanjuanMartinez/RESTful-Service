package org.example.banksproject.repositories;

import org.example.banksproject.models.Mortgage;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface MortgageRepository extends CrudRepository<Mortgage, Integer> {

    Optional<Mortgage> findByIdAndBankId(int id, int bankId);

    List<Mortgage> findByBankId(int bankId);

    void deleteByIdAndBankId(int id, int bankId);

    boolean existsByIdAndBankId(int id, int bankId);
}
