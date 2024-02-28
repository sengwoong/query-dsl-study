package com.ExamQueyDsl.DongoShop.MemberTeamTest;

import com.ExamQueyDsl.DongoShop.model.Member;

import com.ExamQueyDsl.DongoShop.model.QMember;
import com.ExamQueyDsl.DongoShop.model.Team;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
public class DslVsJPQL {

    @PersistenceContext
    EntityManager em;

    @BeforeEach
    public void before() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }


    @Test
    public void startJPQL() {
        //member1을 찾아라.
        String qlString =
                "select m from Member m " +
                        "where m.username = :username";
        List<Member> resultList = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getResultList();

        assertThat(resultList.size()).isEqualTo(2); // 결과가 하나인지 확인

        if (!resultList.isEmpty()) {
            Member findMember = resultList.get(0);
            assertThat(findMember.getUsername()).isEqualTo("member1");
        } else {
            // 결과가 없는 경우에 대한 처리
          return;
        }
    }
    @Test
    public void startQuerydsl() {
        // member1을 찾아라.
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember m = new QMember("m");
        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchFirst(); // fetchOne() 대신 fetchFirst() 사용
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }


}
