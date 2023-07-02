package com.seungh1024.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table
public class Hello {
    @Id
    @GeneratedValue
    private Long id;


}
