package org.example.banksproject.repositories;

import org.example.banksproject.models.Bank;
import org.springframework.data.repository.CrudRepository;

public interface BankRepository extends CrudRepository<Bank, Integer> {
}
