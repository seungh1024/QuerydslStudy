package com.seungh1024.repository;

import com.seungh1024.dto.MemberSearchCondition;
import com.seungh1024.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
    Page<MemberTeamDto> searchPage(MemberSearchCondition condition, Pageable pageable);
//    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
