package study.datajpa.repository;

import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();

    //사용자 정의 리포지토리 구현
    //  스프링 데이터 JPA 리포지토리는 인터페이스만 정의하고 구현체는 스프링이 자동 생성
    //  스프링 데이터 JPA 가 제공하는 인터페이스를 직접 구현하면 구현해야 하는 기능이 너무 많음
    //  다양한 이유로 인터페이스의 메서드를 직접 구현하고 싶다면?
    //      JPA 를 직접 사용 (EntityManager)
    //      스프링 JDBC Template 사용
    //      MyBatis 사용
    //      데이터베이스 커넥션 직접 사용 등등.....
    //      QueryDSL 사용
    //  규칙 -> 리포지토리 인터페이스 이름 + Impl
    //  스프링 데이터 JPA 가 인식해서 스프링 빈으로 등록

    //  실무에서는 주로 QueryDSL 이나 SpringJdbcTemplate 을 함께 사용할 때 사용자 정의 리포지토리 기능 주로 사용
    //  항상 사용자 정의 리포지토리가 필요한 것은 아니다. 그냥 임의의 리포지토리를 만들어도 된다.
    //  예를 들어 MemberQueryRepository 를 인터페이스가 아닌 클래스로 만들고 스프링 빈으로 등록해서 직접 사용해도 된다.
    //  물론 이와 같은 경우 스프링 데이터 JPA 와는 아무런 관계 없이 별도로 동작한다.
}
