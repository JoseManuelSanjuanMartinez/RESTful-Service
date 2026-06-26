package org.example.banksproject.repositories;

import org.example.banksproject.models.ApiUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ApiUsersRepository extends CrudRepository<ApiUser, Integer> {
    Optional<ApiUser> findByApiKey(String apiKey);
}

