package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDTO;
import study.querydsl.dto.QMemberTeamDTO;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;


public class MemberRepositoryImpl extends QuerydslRepositorySupport implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        super(Member.class);
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDTO> search(MemberSearchCondition condition) {
       /* return select(new QMemberTeamDTO(
                member.id,
                member.username,
                member.age,
                team.id,
                team.name
        ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();*/

        return from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .select(new QMemberTeamDTO(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .fetch();
    }

    //전체 카운트를 한번에 조회하는 단순한 방법
    //  searchPageSimple(), fetchResults() 사용
    //  Querydsl 이 제공하는 fetchResults() 를 사용하면 내용과 전체 카운트를 한번에 조회할 수 있다. (실제 쿼리는 2번 호출)
    //  fetchResult() 는 카운트 쿼리 실행시 필요없는 order by 는 제거한다.
    @Override
    public Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDTO> results =
                select(new QMemberTeamDTO(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                        .from(member)
                        .leftJoin(member.team, team)
                        .where(
                                usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe()))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetchResults();

        JPQLQuery<MemberTeamDTO> jpaQuery =
                from(member)
                        .leftJoin(member.team, team)
                        .where(
                                usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe()))
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
                        .select(new QMemberTeamDTO(
                                member.id,
                                member.username,
                                member.age,
                                team.id,
                                team.name
                        ));

        JPQLQuery<MemberTeamDTO> query = getQuerydsl().applyPagination(pageable, jpaQuery);

        List<MemberTeamDTO> content = results.getResults();
        long totalCount = results.getTotal();
        return new PageImpl<>(content, pageable, totalCount);

    }

    //데이터 내용과 전체 카운트를 별도로 조회하는 방법
    //  전체 카운트를 조회하는 방법을 최적화 할 수 있으면 이렇게 분리하면 된다.
    //      (예를 들어 전체 카운트를 조회할 때 조인 쿼리를 줄일 수 있다면 상당한 효과가 있다.)
    //  코드를 리펙토링해서 내용 쿼리와 카운트 쿼리를 읽기 좋게 분리하면 좋다.
    @Override
    public Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDTO> content = queryFactory
                .select(new QMemberTeamDTO(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Member> countQuery = queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()));

//        return new PageImpl<>(content, pageable, totalCount);
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
        //스프링 데이터 페이징 활용2 - CountQuery 최적화
        //  스프링 데이터 라이브러리가 제공
        //  count 쿼리가 생략 가능한 경우 생략해서 처리
        //      페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
        //      마지막 페이지 일때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함)
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
