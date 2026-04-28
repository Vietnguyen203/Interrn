package com.vietnl.sharedlibrary.data.jpa.query;

import jakarta.persistence.EntityManager;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JpaFactory {

  @Getter private static EntityManager entityManager;

  @Autowired
  public void setEntityManager(EntityManager entityManager) {
    JpaFactory.entityManager = entityManager;
  }
}
