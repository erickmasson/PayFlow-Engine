package com.payflow_engine.domain.repositories;

import com.payflow_engine.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByCpfCnpj(String cpfCnpj);
}
