package com.manager.account.domain.models.entities;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * Base class cho auditing với JPA.
 * Cần bật @EnableJpaAuditing trong Application.
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Base {

    @CreatedDate
    @Column(name = "CREATED_DATE", updatable = false)
    private Date createdDate;

    @CreatedBy
    @Column(name = "CREATED_BY", updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "UPDATED_DATE")
    private Date updatedDate;

    @LastModifiedBy
    @Column(name = "UPDATED_BY")
    private String updatedBy;
}
