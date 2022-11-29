package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDTO;
import study.datajpa.entity.Member;

import javax.persistence.Column;
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

}
