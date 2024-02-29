package com.ExamQueyDsl.DongoShop.MemberTeamTest;

import com.ExamQueyDsl.DongoShop.dto.MemberDto;
import com.ExamQueyDsl.DongoShop.dto.QMemberDto;
import com.ExamQueyDsl.DongoShop.dto.UserDto;
import com.ExamQueyDsl.DongoShop.model.Member;

import com.ExamQueyDsl.DongoShop.model.QMember;
import com.ExamQueyDsl.DongoShop.model.QTeam;
import com.ExamQueyDsl.DongoShop.model.Team;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.*;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.ExamQueyDsl.DongoShop.model.QMember.member;
import static com.ExamQueyDsl.DongoShop.model.QTeam.team;

@SpringBootTest
@Transactional
public class DslVsJPQL {

    @PersistenceContext
    EntityManager em;
    @PersistenceUnit
    EntityManagerFactory emf;
    private Object queryFactory;

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

// 런타임에러 단점
    @Test
    public void startJPQL() {
        //member1을 찾아라.
        String qlString =
                "select m from Member m " +
                        "where m.username = :username";
        List<Member> resultList = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getResultList();

        assertThat(resultList).isNotNull(); // 결과가 있는지 확인

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

    @Test
    public void startQuerydsl2() {
        // member1을 찾아라.


        QMember member = QMember.member;

        member.username.eq("member1"); // username = 'member1'
        member.username.ne("member1"); //username != 'member1'
        member.username.eq("member1").not(); // username != 'member1'
        member.username.isNotNull(); //이름이 is not null
        member.age.in(10, 20); // age in (10,20)
        member.age.notIn(10, 20); // age not in (10, 20)
        member.age.between(10,30); //between 10, 30
        member.age.goe(30); // age >= 30
        member.age.gt(30); // age > 30
        member.age.loe(30); // age <= 30
        member.age.lt(30); // age < 30
        member.username.like("member%"); //like 검색
        member.username.contains("member"); // like ‘%member%’ 검색
        member.username.startsWith("member"); //like ‘member%’ 검색


    }


    @Test
    public void searchAndParam() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Member> result1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"),member.age.eq(10))
                .fetch();
        assertThat(result1.size()).isEqualTo(1);


        //List
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();
        //단 건
        Member findMember1 = queryFactory
                .selectFrom(member)
                .fetchFirst();
        //처음 한 건 조회
        Member findMember2 = queryFactory
                .selectFrom(member)
                .fetchFirst();
        List<Member> results = queryFactory
                .selectFrom(member)
                .fetch(); // fetch() 메서드를 사용하여 페이징 쿼리 실행
        //https://www.inflearn.com/questions/806452/querydsl-5-0-0-%EA%B8%B0%EC%A4%80%EC%9C%BC%EB%A1%9C-%EA%B0%95%EC%9D%98-%EB%82%B4%EC%9A%A9%EC%9D%84-%EC%A0%95%EB%A6%AC%ED%96%88%EB%8A%94%EB%8D%B0-%EC%98%AC%EB%B0%94%EB%A5%B4%EA%B2%8C-%EC%9D%B4%ED%95%B4%ED%95%9C-%EA%B2%83%EC%9D%BC%EA%B9%8C%EC%9A%94
       // 추가로 Querydsl 5.0과 무관하게 size()를 사용하면 안됩니다! 이것은 성능상 네트워크를 통한 데이터 전송이 매우 많기 때문에 OOM 장애로 이어질 수 있습니다. 데이터가 1000만 건이라면 성능도 문제이지만 메모리에 다 올리지 못하고 바로 장애가 발생하겠지요?
        // count 쿼리 실행
        Long count = queryFactory
                .select(member.count())
                .fetchOne();



        // fetchCount() 메서드를 사용하여 count 쿼리 실행
        System.out.println("results");
        System.out.println(results);
        System.out.println(results);
        System.out.println(results);
        assertThat(results).isNotNull(); // 결과가 비어있지 않은지 확인
        assertThat(count).isEqualTo(4); // 결과 수와 기대 값이 일치하는지 확인

    }


    @Test
    public void searchAndParam2() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Member> result1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"),member.age.eq(10))
                .fetch();
        assertThat(result1.size()).isEqualTo(1);

        //List
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();
        //단 건
        Member findMember1 = queryFactory
                .selectFrom(member)
                .fetchOne();
        //처음 한 건 조회
        Member findMember2 = queryFactory
                .selectFrom(member)
                .fetchFirst();
        //페이징에서 사용
        List<Member> results = queryFactory
                .selectFrom(member)
                .offset(3)
                .limit(1)
                .fetch();




        //count 쿼리로 변경
        Long count = queryFactory
                .select(member.count())
                .fetchOne();


    }


    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }



    @Test
    public void paging1() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //0부터 시작(zero index)
                .limit(2) //최대 2건 조회
                .fetch();
        assertThat(result.size()).isEqualTo(2);
    }


    /**
     * JPQL
     * select
     *COUNT(m),
     //회원수
     *SUM(m.age), //나이 합
     *AVG(m.age), //평균 나이
     *MAX(m.age), //최대 나이
     *MIN(m.age)
     //최소 나이
     * from Member m
     */
    @Test
    public void aggregation() throws Exception {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();
        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }



    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /**
     * 팀 A에 소속된 모든 회원
     *
     */
    @Test
    public void join() throws Exception {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }


    /**
     * 세타 조인(연관관계가 없는 필드로 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .fetch();
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }



    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and
     t.name='teamA'
     */
    @Test
    public void join_on_filtering() throws Exception {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);}
    }


//    t=[Member(id=3, username=member1, age=10), Team(id=1, name=teamA)]
//    t=[Member(id=4, username=member2, age=20), Team(id=1, name=teamA)]
//    t=[Member(id=5, username=member3, age=30), null]
//    t=[Member(id=6, username=member4, age=40), null]


    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM
     Member m LEFT JOIN Team t ON m.username = t.name
     */
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("t=" + tuple);
        }}


//    t=[Member(id=3, username=member1, age=10), null]
//    t=[Member(id=4, username=member2, age=20), null]
//    t=[Member(id=5, username=member3, age=30), null]
//    t=[Member(id=6, username=member4, age=40), null]
//    t=[Member(id=7, username=teamA, age=0), Team(id=1, name=teamA)]
//    t=[Member(id=8, username=teamB, age=0), Team(id=2, name=teamB)]


    @Test
    public void fetchJoinUse() throws Exception {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        em.flush();
        em.clear();
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();
        boolean loaded =
                emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }


    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() throws Exception {QMember memberSub = new QMember("memberSub");
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(40);
    }


    /**
     * 서브쿼리 여러 건 처리, in 사용
     */
    @Test
    public void subQueryIn() throws Exception {
        QMember memberSub = new QMember("memberSub");
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);

// -----
        List<Tuple> fetch = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ).from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " +
                    tuple.get(JPAExpressions.select(memberSub.age.avg())
                            .from(memberSub)));
        }

// -----
        List<Member> result3 = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)))
                .fetch();

        ///----

        List<String> result4 = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        // ----
        List<String> result5 = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();





//        예를 들어서 다음과 같은 임의의 순서로 회원을 출력하고 싶다면?
//                1. 0 ~ 30살이 아닌 회원을 가장 먼저 출력
//        2. 0 ~ 20살 회원 출력
//        3. 21 ~ 30살 회원 출력

        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);
        List<Tuple> result6 = queryFactory
                .select(member.username, member.age, rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();
        for (Tuple tuple : result6) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = " +
                    rank);
        }

        Tuple result7 = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetchFirst();


        String result8 = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

//-- 들고오는게하
        List<String> result9 = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
// -- 다들고오

        List<Tuple> result10 = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();
        for (Tuple tuple : result10) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username=" + username);
    }


//--- 결과룰 dto에 반환
        List<MemberDto> result11 = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        // 세터없이 바로
        List<MemberDto> result12 = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

// 타입체크
        List<MemberDto> result13 = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
// 하나수정해서생

        List<UserDto> fetch14 = queryFactory
                .select(Projections.fields(UserDto.class,member.username.as("name"),
                                ExpressionUtils.as(
                                        JPAExpressions
                                                .select(memberSub.age.max())
                                                .from(memberSub), "age")
                        )
                ).from(member)
                .fetch();

//QMemberDto 로만들어 생산

        List<MemberDto> result15 = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

//distinct 중복제거
        List<String> result16= queryFactory
                .select(member.username).distinct()
                .from(member)
                .fetch();

        //

    }


    @Test
    public void 동적쿼리_BooleanBuilder() throws Exception {


        String usernameParam = "member1";Integer ageParam = 10;
        List<Member> result = searchMember1(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }
    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }
        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

//--- Where 다중 파라미터 사용
    @Test
    public void 동적쿼리_WhereParam() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;
        List<Member> result = searchMember2(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        return queryFactory
                .selectFrom(member)
//                .where(usernameEq(usernameCond), ageEq(ageCond))
                .where(allEq(usernameCond,ageCond))
                .fetch();
    }
    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;}
    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }


    private BooleanExpression allEq(String usernameCond,Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }


    @Test
    public void 수정삭제_벌크연산() throws Exception {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();


        long count2 = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();



        long count3 = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();


        String result = queryFactory
                .select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})",
                        member.username, "member", "M"))
                .from(member)
                .fetchFirst();

        String 소문자변경해서비교= queryFactory.select(member.username)
                .from(member)
                .where(member.username.eq(Expressions.stringTemplate("function('lower', {0})",
                        member.username)))
//                .where(member.username.eq(member.username.lower()))(위와같음)
                .fetchFirst();


    }
}
