package org.example.banksproject.repositories;

import org.example.banksproject.models.Mortgage;
import org.springframework.data.repository.CrudRepository;

public interface MortgagesRepository extends CrudRepository<Mortgage, Integer> {
}
