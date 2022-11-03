package jpql;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            //JPQL (Java Persistence Query Language)
            //  JPQL - 기본 문법과 기능
            //      JPQL 은 객체지향 쿼리 언어이다. 따라서 테이블을 대상으로 쿼리하는 것이 아니라 엔티티 객체를 대상으로 쿼리한다.
            //      JPQL 은 SQL 을 추상화해서 특정 DB SQL 에 의존하지 않는다.
            //      JPQL 은 결국 SQL 로 변환된다.

            //  JPQL 문법
            //      select m from Member as m where m.age>18
            //      엔티티와 속성은 대소문자 구분해야함 (Member, age)
            //      JPQL 키워드는 대소문자 구분하지 않음 (SELECT, FROM, where)
            //      엔티티 이름 사용, 테이블 이름이 아님 (Member)
            //      별칭은 필수 (m) (as는 생략 가능)

            //  집합과 정렬
            //      select
            //          count(m),       //회원수
            //          sum(m.age),     //나이 합
            //          avg(m.age),     //평균 나이
            //          max(m.age),     //최대 나이
            //          min(m.age)      //최소 나이
            //      from Member m
            //      group by, having, order by -> SQL 이랑 똑같이 사용하면 됨

            //  TypeQuery, Query
            //      TypeQuery -> 반환 타입이 명확할 때 사용
            //          TypedQuery<Member> query = em.createQuery("select m from Member m", Member.class);
            //      Query -> 반환 타입이 명확하지 않을 때 사용
            //          Query query = em.createQuery("select m.username, m.age from Member m");

            //  결과 조회 API
            //      query.getResultList(): 결과가 하나 이상일 때, 리스트 반환 / 없으면 빈 리스트 반환
            //      query.getSingleResult(): 결과가 정확히 하나, 단일 객체 반환 / 없으면 NoResultException / 둘 이상이면 NonUniqueResultException
//            Member member = new Member();
//            member.setUsername("memberA");
//            member.setAge(19);
//            em.persist(member);
//
//            //파라미터 바인딩 - 이름 기준, 위치 기준 (위치 기준은 그냥 쓰지말자)
//            Member singleResult = em.createQuery("select m from Member m where m.username= :username", Member.class)
//                    .setParameter("username", "memberA")
//                    .getSingleResult();
//
//            System.out.println("singleResult = " + singleResult);

            //프로젝션
            //  SELECT 절에 조회할 대상을 지정하는 것
            //  프로젝션 대상: 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자 등 기본 데이터 타입)
            //  SELECT m FROM Member m -> 엔티티 프로젝션 / 영속성 컨텍스트에서 관리
            //  SELECT m.team FROM Member m -> 엔티티 프로젝션 (근데 이렇게 쓰면 안됨(묵시적 조인))
            //      => SELECT t FROM Member m join m.team t (이렇게 가져와야 됨 (명시적 조인))
            //  SELECT m.address FROM Member m -> 임베디드 타입 프로젝션
            //  SELECT m.username, m.age FROM Member m -> 스칼라 타입 프로젝션
            //  DISTINCT 로 중복 제거
            //      => SELECT DISTINCT m.username, m.age FROM Member m

            //프로젝션 - 여러 값 조회
            //  SELECT m.username, m.age FROM Member m
            //  1. Query 타입으로 조회
            //  2. Object[] 타입으로 조회
//            List resultList = em.createQuery("select m.username, m.age from Member m")
//                    .getResultList();
//            Object o = resultList.get(0);
//            Object[] result = (Object[]) o;
//            System.out.println("username = " + result[0]);
//            System.out.println("age = " + result[1]);
            //  3. new 명령어로 조회 (이게 제일 깔끔함)
            //      단순 값을 DTO 로 바로 조회
//            List<MemberDTO> resultList = em.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member m", MemberDTO.class)
//                    .getResultList();
            //      패키지 명을 포함한 전체 클래스명 입력
            //      순서와 타입이 일치하는 생성자 필요

            //페이징 API
            //  JPA 는 페이징을 다음 두 API 로 추상화한다.
            //  setFirstResult(int startPosition): 조회 시작 위치(0부터 시작)
            //  setMaxResults(int maxResult): 조회할 데이터 수
//            List<Member> resultList = em.createQuery("select m from Member m order by m.username desc", Member.class)
//                    .setFirstResult(10)     //10번째 부터
//                    .setMaxResults(20)      //20개를 가져오기
//                    .getResultList();

            //조인
            //  내부 조인: SELECT m FROM Member m [INNER] JOIN m.team t
            //  외부 조인: SELECT m FROM Member m LEFT [OUTER] JOIN m.team t
            //  세타 조인: SELECT count(m) from Member m, Team t where m.username = t.name

            Team team = new Team();
            team.setName("teamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("memberA");
            member.setAge(10);
            member.setTeam(team);
            em.persist(member);

            //Inner Join
            String innerJoinQuery = "select m from Member m inner join m.team t";
            List<Member> resultListInner = em.createQuery(innerJoinQuery, Member.class)
                    .getResultList();

            //Outer Join
            String outerJoinQuery = "select m from Member m left join m.team t";
            List<Member> resultListOuter = em.createQuery(outerJoinQuery, Member.class)
                    .getResultList();

            //Theta Join (걍 막 조인)
            String thetaJoinQuery = "select m from Member m, Team t where m.username = t.name";
            List<Member> resultListTheta = em.createQuery(thetaJoinQuery, Member.class)
                    .getResultList();

            //조인 - ON 절
            //  ON 절을 활용한 조인
            //  1. 조인 대상 필터링
            //      ex) 회원과 팀을 조인하면서, 팀 이름이 A인 팀만 조인
            //          => JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'A'
            //          => SQL:  SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID = t.id and t.name = 'A'
            //  2. 연관관계 없는 엔티티 외부 조인
            //      ex) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
            //          => JPQL: SELECT m, t FROM Member m LEFT JOIN Team t ON m.username = t.name
            //          => SQL:  SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        }finally {
            em.close();
        }
        emf.close();
    }
}
