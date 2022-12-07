package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDTO;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember(){
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> members = memberRepository.findAll();
        assertThat(members.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void findByUsernameAndAgeGreaterThan() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member1", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("member1", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("member1");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery(){
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByUsername("member1");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    public void testQuery(){
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findUser("member1", 10);
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    public void findUsernameList() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("username = " + s);
        }
    }

    @Test
    public void findMemberDTO(){
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member = new Member("member", 10);
        memberRepository.save(member);

        List<MemberDTO> memberDTO = memberRepository.findMemberDTO();
        for (MemberDTO dto : memberDTO) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("member1", "member2"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }
    @Test
    public void returnType() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> findListMember = memberRepository.findListByUsername("member1");
        Member findMember = memberRepository.findMemberByUsername("member1");
        Optional<Member> findOptionalMember = memberRepository.findOptionalByUsername("member1");
    }

    @Test
    public void paging() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        //실무 꿀팁: 엔티티 반환 절대X -> Page<Member> => Page<MemberDTO> 로 변환하기
        Page<MemberDTO> memberDTOPage = page.map(member -> new MemberDTO(member.getId(), member.getUsername(), null));

        List<Member> content = page.getContent();               //조회된 데이터
        long totalElements = page.getTotalElements();           //전체 데이터 수

        assertThat(content.size()).isEqualTo(3);        //조회된 데이터 수
        assertThat(totalElements).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);      //페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2);  //전체 페이지 번호
        assertThat(page.isFirst()).isTrue();                    //첫번째 항목인가?
        assertThat(page.hasNext()).isTrue();                    //다음 페이지가 있는가?
    }

    @Test
    public void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        int resultCount = memberRepository.bulkAgePlus(20);
//        em.clear();
        //DB 에는 1 증가로 반영, 영속성 컨텍스트에는 아직 반영안됨
        //벌크 연산 이후에는 항상 영속성 컨텍스트를 초기화해주어야 함
        //@Modifying 옵션에서 clearAutomatically = true 를 주면 자동 초기화

        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);

        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamB);

        em.flush();
        em.clear();

        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }
    }

    @Test
    public void queryHint() {
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        Member findMember = memberRepository.findById(member1.getId()).get();
        //findMember.setUsername("member2");
    }

    @Test
    public void callCustom() {
        List<Member> result = memberRepository.findMemberCustom();
    }

    @Test
    public void queryByExample() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member member1 = new Member("member1", 0, teamA);
        Member member2 = new Member("member2", 0, teamA);
        em.persist(member1);
        em.persist(member2);

        em.flush();
        em.clear();

        //Probe
        Member member = new Member("member1");

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("age");

        Example<Member> example = Example.of(member, matcher);
        List<Member> result = memberRepository.findAll(example);

        assertThat(result.get(0).getUsername()).isEqualTo("member1");
    }
    //QueryByExample
    //  장점
    //      동적 쿼리를 편리하게 처리
    //      도메인 객체를 그대로 사용
    //      데이터 저장소를 RDB 에서 NOSQL 로 변경해도 코드 변경이 없게 추상화되어 있음
    //      스프링 데이터 JPA JpaRepository 인터페이스에 이미 포함
    //  단점
    //      조인은 가능하지만 내부 조인(Inner Join)만 가능함. 외부 조인(Left Join)은 어림도 없음
    //      다음과 같은 중첩 제약 조건 안됨(firstname = ?0 or (firstname = ?1 and lastname = ?2)
    //      매칭 조건이 매우 단순함
    //          문자는 starts/contains/ends/regex
    //          다른 속성은 정확한 매칭( = )만 지원
    //  결론
    //      실무에서 사용하기에는 매칭 조건이 너무 단순하고, LEFT 조인이 안됨
    //      걍 갓QueryDSL 을 쓰자. QueryDSL 이 짱임

    @Test
    public void projections() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member member1 = new Member("member1", 0, teamA);
        Member member2 = new Member("member2", 0, teamA);
        em.persist(member1);
        em.persist(member2);

        em.flush();
        em.clear();

        List<UsernameOnly> result = memberRepository.findProjectionByUsername("member1");
        List<UsernameOnly> resultGeneric = memberRepository.findProjectionsGenericByUsername("member1", UsernameOnly.class);
        for (UsernameOnly usernameOnly : result) {
            System.out.println("usernameOnly = " + usernameOnly);
        }
    }
    //Projections
    //  엔티티 대신에 DTO 를 편리하게 조회할 때 사용
    //  전체 엔티티가 아니라 만약 회원 이름만 딱 조회하고 싶으면? 이걸 쓰면됨
    //  조회할 엔티티의 필드를 getter 형식으로 지정하면 해당 필드만 선택해서 조회(Projection)
    //  SQL 절에서도 select 절에서 username 만 조회(Projection) 하는 것을 확인

    //  인터페이스 기반 Closed Projections
    //      public interface UsernameOnly {
    //          String getUsername();
    //      }
    //  프로퍼티 형식(getter)의 인터페이스를 제공하면, 구현체는 스프링 데이터 JPA 가 제공

    //  인터페이스 기반 Open Projections
    //      public interface UsernameOnly {
    //          @Value("#{target.username + ' ' + target.age + ' ' + target.team.name}")
    //          String getUsername();
    //      }
    //  이와 같이 스프링의 SpEL 문법도 지원
    //  하지만 SpEL 문법을 사용시, DB 에서 엔티티 필드를 다 조회해온 다음에 계산한다. 따라서 JPQL SELECT 절 최적화가 안된다.

    //  클래스 기반 Projection
    //      @Getter
    //      public class UsernameOnlyDTO {
    //          private final String username;
    //
    //          public UsernameOnlyDTO(String username) {
    //              this.username = username;
    //          }
    //      }
    //  다음과 같이 인터페이스가 아닌 구체적인 DTO 형식도 가능
    //  생성자의 파라미터 이름으로 매칭

    //  동적 Projection
    //      프로젝션 대상이 root 엔티티면, JPQL Select 절 최적화 가능
    //      프로젝션 대상이 root 가 아니면
    //          Left Outer Join 처리
    //          모든 필드를 Select 해서 엔티티로 조회한 다음에 계산
    //  결론
    //      프로젝션 대상이 root 엔티티면 유용하다.
    //      프로젝션 대상이 root 엔티티를 넘어가면 JPQL Select 최적화가 안된다!
    //      실무의 복잡한 쿼리를 해결하기에는 한계가 있다.
    //      실무에서는 단순할 때만 사용하고, 조금만 복잡해지면 갓갓QueryDSL 을 사용하자

    @Test
    public void nativeQuery() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member member1 = new Member("member1", 0, teamA);
        Member member2 = new Member("member2", 0, teamA);
        em.persist(member1);
        em.persist(member2);

        em.flush();
        em.clear();

        Member result = memberRepository.findByNativeQuery("member1");
        System.out.println("result = " + result);

        //Projections 활용
        Page<MemberProjection> resultByNativeProjection = memberRepository.findByNativeProjection(PageRequest.of(0, 10));
        List<MemberProjection> content = resultByNativeProjection.getContent();
        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection.getUsername() = " + memberProjection.getUsername());
            System.out.println("memberProjection.getTeamname() = " + memberProjection.getTeamname());

        }
    }
    //네이티브 쿼리
    //  가급적 네이티브 쿼리는 사용하지 않는게 좋음, 정말 어쩔 수 없을 때 사용
    //  최근에 나온 궁극의 방법 -> 스프링 데이터 Projections 활용

    //  스프링 데이터 JPA 기반 네이티브 쿼리
    //      페이징 지원
    //      반환 타입: Object[] / Tuple / DTO(스프링 데이터 인터페이스 Projections 지원)
    //      제약
    //          Sort 파라미터를 통한 정렬이 정상 동작하지 않을 수 있음(믿지 말고 직접 처리
    //          JPQL 처럼 애플리케이션 로딩 시점에 문법 확인 불가
    //          동적 쿼리 불가

    //  JPA 네이티브 SQL 지원
    //      JPQL 은 위치 기반 파라미터를 1부터 시작하지만 네이티브 SQL 은 0부터 시작
    //      네이티브 SQL 을 엔티티가 아닌 DTO 로 변환은 하려면
    //          DTO 대신 JPA Tuple 조회
    //          DTO 대신 Map 조회
    //          @SqlResultSetMapping -> 복잡
    //          Hibernate ResultTransformer 를 사용해야함 -> 복잡
    //          결론: 네이티브 SQL 을 DTO 로 조회할 때는 JdbcTemplate or myBatis 권장

    //  Projections 활용
    //      스프링 데이터 JPA 네이티브 쿼리 + 인터페이스 기반 Projections 활용
}