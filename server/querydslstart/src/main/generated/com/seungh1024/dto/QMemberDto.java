package com.seungh1024.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.seungh1024.dto.QMemberDto is a Querydsl Projection type for MemberDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMemberDto extends ConstructorExpression<MemberDto> {

    private static final long serialVersionUID = 734952514L;

    public QMemberDto(com.querydsl.core.types.Expression<String> username, com.querydsl.core.types.Expression<Integer> age) {
        super(MemberDto.class, new Class<?>[]{String.class, int.class}, username, age);
    }

}

