package com.vietnl.usersservice.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    private String username;

    @Column(name = "password_hash")
    private String password;

    private Integer role;
    private Integer status;

    private String phoneNumber;
    private String fullName;
    private String email;

    private LocalDateTime birthday;

    private String citizenPid;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
