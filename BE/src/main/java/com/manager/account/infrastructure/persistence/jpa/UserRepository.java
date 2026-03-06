package com.manager.account.infrastructure.persistence.jpa;

import com.manager.account.domain.models.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, String> {
    Optional<Users> findByUsername(String username);

    Optional<Users> findByEmail(String email);

    Optional<Users> findByEmployeeIdAndServer(String employeeId, String server);

    boolean existsByUsername(String username);

    Optional<Users> findByEmployeeId(String employeeId);

    List<Users> findByServer(String server);
}
