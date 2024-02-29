package com.ExamQueyDsl.DongoShop.repository;

import com.ExamQueyDsl.DongoShop.model.Member;
import com.ExamQueyDsl.DongoShop.model.MemberSearchCondition;
import com.ExamQueyDsl.DongoShop.model.MemberTeamDto;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;
import java.util.List;

public interface MemberRepositoryCustom  {
    List<MemberTeamDto> search(MemberSearchCondition condition);





    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition,
                                         org.springframework.data.domain.Pageable pageable);

    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition,
                                          org.springframework.data.domain.Pageable pageable);
}
