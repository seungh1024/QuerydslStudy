package com.seungh1024.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table
public class Hello {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


}
