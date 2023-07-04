package com.seungh1024.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.ToString;

//@Data
@ToString
public class MemberDto {
    private String username;
    private int age;

    public MemberDto(){};

    @QueryProjection
    public MemberDto(String username, int age){
        this.username = username;
        this.age = age;
    }
}
