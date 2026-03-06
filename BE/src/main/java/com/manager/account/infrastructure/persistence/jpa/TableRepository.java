package com.manager.account.infrastructure.persistence.jpa;

import com.manager.account.domain.models.entities.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<Table, String> {
    List<Table> findByServer(String server);

    Optional<Table> findByIdAndServer(String id, String server);

    List<Table> findByServerAndCurrentOrderIdIsNull(String server);
}
