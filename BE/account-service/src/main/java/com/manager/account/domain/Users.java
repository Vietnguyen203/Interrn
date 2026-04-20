package com.manager.account.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@javax.persistence.Table(name = "USERS")
public class Users {
    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "USERNAME")
    private String username;

    @JsonIgnore
    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "EMPLOYEE_ID")
    private String employeeId;

    @Column(name = "SERVER")
    private String server;

    @Column(name = "FULL_NAME")
    private String fullName;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "ROLE")
    private String role;

    @Column(name = "BIRTHDAY")
    private LocalDate birthday;

    /** Giới tính: "MALE", "FEMALE", "OTHER" */
    @Column(name = "GENDER")
    private String gender;
}
