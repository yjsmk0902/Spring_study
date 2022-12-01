package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDTO;
import study.datajpa.entity.Member;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    //간단할 때 쓰기
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
    //메소드 이름으로 쿼리 생성 - 메소드 이름을 분석해서 JPQL 쿼리 실행
    //  스프링 데이터 JPA 가 제공하는 쿼리 메소드 기능
    //      조회: find...By / read...By / query...By / get...By
    //      COUNT: count...By (반환타입 long)
    //      EXISTS: exists...By (반환타입 boolean)
    //      DELETE: delete...By / remove...By (반환타입 long)
    //      DISTINCT: findDistinct / findMemberDistinctBy
    //      LIMIT: findFirst3 / findFirst / findTop / findTop3
    //  이 기능은 엔티티의 필드명이 변경되면 인터페이스에 정의한 메서드 이름도 꼭 함께 변경해야 한다.
    //  그렇지 않으면 애플리케이션을 시작하는 시점에 오류가 발생한다.
    //  이렇게 애플리케이션 로딩 시점에 오류를 인지할 수 있는 것이 스프링 데이터 JPA 의 매우 큰 장점이다.

    //이거보다는 @Query 를 쓰자
    //@Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);
    //JPA NamedQuery - JPA 의 NamedQuery 를 호출할 수 있음
    //  스프링 데이터 JPA 는 선언한 "도메인 클래스 + . + 메서드 이름" 으로 Named 쿼리를 찾아서 실행한다.
    //  만약 실행할 Named 쿼리가 없으면 메서드 이름으로 쿼리 생성 전략을 사용한다.
    //  필요하면 전략을 변경할 수 있지만 권장하지는 않는다.
    //  애플리케이션 로딩 시점에 오류를 잡을 수 있음
    //  + 실무에서는 Named Query 를 직접 등록해서 사용하는 일은 드물다.
    //      대신 @Query 를 사용해서 repository 메소드에 쿼리를 직접 정의한다.

    //이거 자주 쓸거임 마스터해야함
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);
    //@Query, 리포지토리 메소드에 쿼리 정의하기
    //  실행할 메서드에 정적 쿼리를 직접 작성하므로 이름 없는 Named 쿼리라 할 수 있음
    //  JPA Named 쿼리처럼 애플리케이션 실행 시점에 문법 오류를 발견할 수 있음 (매우 큰 장점!!!)
    //  실무에서는 메서드 이름으로 쿼리 생성 기능은 파라미터가 증가할 시 메서드 이름이 매우 복잡해진다.
    //  따라서 @Query 기능을 자주 사용하게 된다.

    @Query("select m.username from Member m")
    List<String> findUsernameList();
    //단순히 값 하나를 조회하기, JPA 값 타입 (@Embedded) 조회하기

    @Query("select new study.datajpa.dto.MemberDTO(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDTO> findMemberDTO();
    //DTO 로 직접 조회하기

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);
    //파라미터 바인딩 - 이름 기반 / 위치 기반 (위치 기반은 거의 안씀)
    //  위에껀 컬렉션 파라미터 바인딩

    //스프링 데이터 JPA 는 유연한 반환 타입을 지원
    List<Member> findListByUsername(String username);           //컬렉션

    Member findMemberByUsername(String username);               //단건

    Optional<Member> findOptionalByUsername(String username);   //단건 Optional
    //  조회 결과가 많거나 없으면?
    //      컬렉션: 결과 없음 -> 빈 컬렉션 반환
    //      단건 조회: 결과 없음 -> null 반환 / 결과가 2건 이상 -> NonUniqueResultException 예외 발생

    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m) from Member m")
        //카운트 쿼리 분리하기
    Page<Member> findByAge(int age, Pageable pageable);
    //스프링 데이터 JPA 페이징과 정렬
    //  Pageable 인터페이스를 활용해서 사용
    //  PageRequest 생성자의 첫 번째 파라미터에는 현재 페이지를, 두 번째 파라미터에는 조회할 데이터 수를 입력한다.
    //  주의: Page 는 1부터 시작이 아니라 0부터 시작이다.
    //  Page (count O)
    //  Slice (count X) 추가로 limit + 1을 조회한다. 그래서 다음 페이지 여부 확인(최근 모바일 리스트처럼)
    //  List (count X)
    //  카운트 쿼리 분리 (이건 복잡한 sql 에서 사용, 데이터는 left join, 카운트는 left join 안해도 됨) 실무에서 중요!!!

    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age+1 where m.age>=:age")
    int bulkAgePlus(@Param("age") int age);
    //벌크성 수정 쿼리
    //  벌크성 수정, 삭제 쿼리는 @Modifying 어노테이션을 사용
    //      사용하지 않으면 다음 예외 발생
    //      ...QueryExecutionRequestException:...
    //  벌크성 쿼리를 실행하고 나서 영속성 컨텍스트 초기화 @Modifying(clearAutomatically = true) : default = false
    //      이 옵션 없이 회원을 find.. 로 다시 조회하면 영속성 컨텍스트에 과거 값이 남아서 문제가 될 수 있다.
    //  +벌크 연산은 영속성 컨텍스트를 무시하고 실행사기 때문에, 영속성 컨텍스트에 있는 엔티티의 상태와 DB 에 엔티티 상태가 달라질 수 있다.
    //      권장 방안
    //          1. 영속성 컨텍스트에 엔티티가 없는 상태에서 벌크 연산을 먼저 실행한다.
    //          2. 부득이하게 영속성 컨텍스트에 엔티티가 있으면 벌크 연산 직후 영속성 컨텍스트를 초기화 한다.

    //EntityGraph
    //  스프링 데이터 JPA 는 JPA 가 제공하는 엔티티 그래프 기능을 편리하게 사용하게 도와준다.
    //  이 기능을 사용하면 JPQL 없이 페치 조인을 사용할 수 있다. (JPQL + 엔티티 그래프도 가능)
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();
    //이와 같이 페치 조인이 필요할 때 매번 JPQL 을 짜주기 귀찮음
    //엔티티 그래프를 사용하여 페치 조인을 편리하게 할 수 있다.

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();
    //공통 메서드 오버라이드

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();
    //JPQL + 엔티티 그래프

    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(String username);
    //메서드 이름으로 쿼리에서 특히 편리하다.

    //JPA Hint & Lock
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);
    //  org.springframework.data.jpa.repository.QueryHints 어노테이션을 사용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);
    //  JPA 가 제공하는 락은 JPA 책 16.1 트랜잭션과 락 절을 참고
}
