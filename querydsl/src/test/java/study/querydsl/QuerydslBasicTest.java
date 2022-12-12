package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        //만약 JPAQueryFactory 를 필드로 제공하면 동시성 문제는??   고민하지 않아도 됨 가능가능
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
        String qlString = "select m from Member m " +
                "where m.username = :username";

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {

        //기본 Q-Type 활용
        QMember m = new QMember("m");   //별칭 직접 지정
        QMember qMember = member;              //기본 인스턴스 사용

        Member findMember = queryFactory
                .selectFrom(m)
                .where(m.username.eq("member1"))
                .fetchOne();

        Member findMemberQ = queryFactory
                .selectFrom(member)
                .where(m.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1").and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();   //리스트 조회, 데이터 없으면 빈 리스트 반환

        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();    //단 건 조회 없으면 null, 둘 이상이면 NonUniqueResultException

        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();  //limit(1).fetchOne();

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();    //페이징 정보 포함, total count 쿼리 추가 실행
        results.getTotal();
        List<Member> content = results.getResults();

        long count = queryFactory
                .selectFrom(member)
                .fetchCount();  //count 쿼리로 변경해서 count 수 조회
    }

    //정렬
    //  회원 정렬 순서
    //      1. 회원 나이 내림차순(desc)
    //      2. 회원 나이 올림차순(asc)
    //      단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
    //  desc, asc -> 일반 정렬 / nullsLast, nullsFirst -> null 데이터 순서 부여
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> findMember = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = findMember.get(0);
        Member member6 = findMember.get(1);
        Member memberNull = findMember.get(2);


    }

    //페이징
    //  조회 건수 제한
    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)  //0부터 시작(zero index)
                .limit(2)   //최대 2건 조회
                .fetch();
        assertThat(result.size()).isEqualTo(2);
    }

    //  전체 조회 수가 필요하다면?
    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }
    //  +실무에서는 페이징 쿼리를 작성할 때, 데이터를 조회하는 쿼리는 여러 테이블을 조인해야 하지만,
    //  count 쿼리는 조인이 필요 없는 경우도 있다. 그런데 이렇게 자동화된 커리는 원본 쿼리와 같이
    //  모두 조인을 해버리기 때문에 성능이 안나올 수 있다. count 쿼리에 조인이 필요 없는 성능 최적화가 필요하다면,
    //  count 전용 쿼리를 별도로 작성해야 한다.

    //집합
    //  COUNT(m)    //회원수
    //  SUM(m.age)  //나이 합
    //  AVG(m.age)  //나이 평균
    //  MAX(m.age)  //최대 나이
    //  MIN(m.age)  //최소 나이
    @Test
    public void aggregation() {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())       //고른게 많을 때 querydsl tuple 사용 보통은 Dto로 뽑아옴
                .from(member)
                .fetch();
        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    //  팀의 이름과 각 팀의 평균 연령을 구해라.
    @Test
    public void group() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)     //그룹화된 결과를 제한하려면 having() 사용
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    //조인
    //  기본 조인 - 조인의 기본 문법은 첫 번째 파라미터에 조인 대상을 지정하고,
    //           두 번째 파라미터에 별칭으로 사용할 Q 타입을 지정하면 된다.
    //      팀 A 에 소속된 모든 회원
    @Test
    public void join() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }
    //  세타 조인 - 연관관계가 없는 필드로 조인
    //      회원의 이름이 팀 이름과 같은 회원 조회
    @Test
    public void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }
    //      from 절에 여러 엔티티를 선택해서 세타 조인
    //      외부 조인 불가능 -> 다음에 설명할 조인 on 을 사용하면 외부 조인 가능

    //  조인 - on 절
    //      조인 대상 필터링 / 연관관계 없는 엔티티 외부 조인
    //      회원과 팀을 조인하면서, 팀 이름이 teamA 인 팀만 조인, 회원은 모두 조회
    //          -> JPQL: select m,t  from Member m left join m.team t on t.name = 'teamA'
    @Test
    public void join_on_filtering() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }
    //      +on 절을 활용해 조인 대상을 필터링 할 때, 외부조인이 아니라 내부조인을 사용하면,
    //      where 절에서 필터링 하는 것과 기능이 동일하다. 따라서 on 절을 활용한 조인 대상 필터링을 사용할 때,
    //      내부조인이면 익숙한 where 절로 해결하고, 정말 외부조인이 필요한 경우에만 이기능을 사용하자.

    //  연관관계 없는 엔티티 외부 조인
    //      회원의 이름과 팀의 이름이 같은 대상 외부 조인
    //          ->JPQL: select m, t from Member m left join Team t on m.username = t.name
    @Test
    public void join_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }
    //      +hibernate 5.1 부터 on 을 사용해서 서로 관계가 없는 필드를 외부 조인하는 기능이 추가되었다.
    //      물론 내부 조인도 가능하다. leftJoin() 부분에 일반 조인과 다르게 엔티티 하나만 들어간다.

    //  조인 - 페치 조인
    @PersistenceUnit
    EntityManagerFactory emf;

    //      페치 조인 미적용
    @Test
    public void fetchJoinNo() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();

    }

    //      페치 조인 적용
    @Test
    public void fetchJoinUse() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team,team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isTrue();
    }

    //서브 쿼리
    //  서브 쿼리 eq 사용
    //      나이가 가장 많은 회원 조회
    @Test
    public void subQuery() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    //  서브 쿼리 goe 사용
    //      나이가 평균 이상인 회원
    @Test
    public void subQueryGoe() {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    //  서브 쿼리 여러건 처리 in 사용
    @Test
    public void subQueryIn() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    //  select 절에 서브 쿼리
    @Test
    public void selectSubQuery() {
        QMember memberSub = new QMember("memberSub");
        List<Tuple> result = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }
    //      JPAExpression -> static import 가능
    //      +from 절의 서브쿼리 한계
    //          JPA JPQL 서브쿼리의 한계점으로 from 절의 서브 쿼리는 지원하지 않는다. 당연히 Querydsl 도
    //          지원하지 않는다. hibernate 구현체를 사용하면 select 절의 서브쿼리는 지원한다.
    //          Querydsl 도 hibernate 구현체를 사용하면 select 절의 서브쿼리를 지원한다.

    //      +from 절의 서브쿼리 해결방안
    //          1. 서브쿼리를 join 으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
    //          2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
    //          3. nativeSQL 을 사용한다.

    //Case 문
    //  select, where, orderBy 에서 사용 가능
    //      단순한 조건
    @Test
    public void basicCaseSimple() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    //      복잡한 조건
    @Test
    public void basicCaseComplex() {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0,20)).then("0~20살")
                        .when(member.age.between(21,30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    //상수, 문자 더하기
    //  상수가 필요하면 Expressions.constant(xxx) 사용
    @Test
    public void constant() {
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }
    //      +위와 같이 최적화가 가능하면 SQL 에 constant 값을 넘기지 않는다.
    //      상수를 더하는 것처럼 최적화가 어려우면 SQL 에 constant 값을 넘긴다.

    //  문자 더하기 concat
    @Test
    public void concat() {
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }
    //      +member.age.stringValue() 부분이 중요한데, 문자가 아닌 다른 타입들은 stringValue() 로
    //      문자로 변환할 수 있다. 이 방법은 ENUM 을 처리할 때도 자주 사용한다.
}
