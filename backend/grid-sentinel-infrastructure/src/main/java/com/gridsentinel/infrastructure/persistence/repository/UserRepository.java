package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.auth.User;
import com.gridsentinel.common.CommonKernel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for User aggregate.
 */
@Repository
public interface UserRepository extends JpaRepository<User, CommonKernel.UserId> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.enabled = true AND (u.lockedUntil IS NULL OR u.lockedUntil < now())")
    List<User> findAllActiveUsers();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}