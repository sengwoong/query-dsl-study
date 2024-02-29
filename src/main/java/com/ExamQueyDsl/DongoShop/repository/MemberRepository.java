package com.ExamQueyDsl.DongoShop.repository;

import com.ExamQueyDsl.DongoShop.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> ,MemberRepositoryCustom{
    List<Member> findByUsername(String username);
}