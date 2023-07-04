package com.seungh1024.controller;

import com.seungh1024.dto.MemberSearchCondition;
import com.seungh1024.dto.MemberTeamDto;
import com.seungh1024.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition){
        System.out.println(condition);
        return memberJpaRepository.searchByParam(condition);
    }
}
