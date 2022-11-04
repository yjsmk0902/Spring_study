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

//            Team team = new Team();
//            team.setName("teamA");
//            em.persist(team);
//
//            Member member = new Member();
//            member.setUsername("memberA");
//            member.setAge(10);
//            member.setTeam(team);
//            em.persist(member);
//
//            //Inner Join
//            String innerJoinQuery = "select m from Member m inner join m.team t";
//            List<Member> resultListInner = em.createQuery(innerJoinQuery, Member.class)
//                    .getResultList();
//
//            //Outer Join
//            String outerJoinQuery = "select m from Member m left join m.team t";
//            List<Member> resultListOuter = em.createQuery(outerJoinQuery, Member.class)
//                    .getResultList();
//
//            //Theta Join (걍 막 조인)
//            String thetaJoinQuery = "select m from Member m, Team t where m.username = t.name";
//            List<Member> resultListTheta = em.createQuery(thetaJoinQuery, Member.class)
//                    .getResultList();

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

            //서브 쿼리 - 쿼리 안에 쿼리
            //  ex) 나이가 평균보다 많은 회원
            //          select m from Member m where m.age > (select avg(m2.age) from Member m2)
            //  ex) 한 건이라도 주문한 고객
            //          select m from Member m where (select count(o) from Order o where m = o.member) > 0
            //  서브 쿼리 지원 함수
            //      [NOT] EXISTS (서브 쿼리): 서브 쿼리에 결과가 존재하면 참
            //          ex) 팀A 소속인 회원
            //              select m from Member m where exists (select t from m.team t where t.name = '팀A')
            //      {ALL | ANY | SOME} (서브 쿼리)
            //          ALL: 모두 만족하면 참
            //          ANY, SOME: 같은 의미, 조건을 하나라도 만족하면 참
            //          ex) 전체 상품 각각의 재고보다 주문량이 많은 주문들
            //              select o from Order o where o.orderAmount > ALL (select p.stockAmount from Product p)
            //          ex) 어떤 팀이든 팀에 소속된 회원
            //              select m from Member m where m.team = ANY(select t from Team t)
            //      [NOT] IN (서브 쿼리): 서브 쿼리의 결과 중 하나라도 같은 것이 있으면 참
            //  JPA 서브 쿼리의 한계
            //      JPA 는 WHERE, HAVING 절에서만 서브 쿼리 사용 가능
            //      SELECT 절도 가능 (하이버네이트에서 지원)
            //      FROM 절의 서브 쿼리는 현재 JPQL 에서 불가능 -> 조인으로 풀 수 있으면 풀어서 해결

            //JPQL 타입 표현
            //  문자: 'HELLO', 'SHE''s'
            //  숫자: 10L(Long), 10D(Double), 10F(Float)
            //  Boolean: TRUE, FALSE
            //  ENUM: jpabook.MemberType.Admin (패키지명 포함)
            //  엔티티 타입: TYPE(m) = Member (상속 관계에서 사용)

            //JPQL 기타
            //  SQL 과 문법이 같은 식
            //  EXISTS, IN
            //  AND, OR, NOT
            //  =, >, >=, <, <=, <>
            //  BETWEEN, LIKE, IS NULL
            //조건식 - CASE 식
            //  기본 CASE 식
            //      select
            //          case when m.age <= 10 then '학생요금'
            //               when m.age >= 60 then '경로요금'
            //          end
            //      from Member m
//            String query = "select " +
//                    "case when m.age <=10 then '학생요금' " +
//                    "when m.age >=60 then '경로요금' " +
//                    "else '일반요금' " +
//                    "end " +
//                    "from Member m";
//            List<String> resultList = em.createQuery(query, String.class)
//                    .getResultList();
            //  단순 CASE 식
            //      select
            //          case t.name
            //              when '팀A' then '인센티브110%'
            //              when '팀B' then '인센티브120%'
            //              else '인센티브105%'
            //          end
            //      from Team t
            //  COALESCE: 하나씩 조회해서 null 이 아니면 반환
            //      ex) select coalesce(m.username, '이름 없는 회원') from Member m
            //  NULLIF: 두 값이 같으면 null 반환, 다르면 첫번째 값 반환
            //      ex) select nullif(m.username, '관리자') as username from Member m

            //JPQL 기본 함수
            //  1. JPQL 표준 함수
            //      CONCAT('a', 'b') -> 문자 두개를 더함
            //      SUBSTRING(m.username, 2, 3) -> 잘라내기
            //      TRIM -> 공백 제거
            //      LOWER, UPPER -> 대소문자
            //      LENGTH -> 문자 길이
            //      LOCATE('de', 'abcdefg') -> 문자열 위치
            //      ABS, SQRT, MOD -> 계산 함수
            //      SIZE, INDEX (JPA 용도) -> 컬렉션의 크기
            //  2. 사용자 정의 함수
            //      사용하는 DB 방언을 상속받고, 사용자 정의 함수를 등록한다.

            //경로 표현식
            //  점을 찍어 객체 그래프를 탐색하는 것
            //  select m.username       -> 상태 필드
            //      from Member m
            //          join m.team t   -> 단일 값 연관 필드
            //          join m.orders o -> 컬렉션 값 연관 필드
            //  where t.name ='팀A'
            //  상태 필드(state field): 단순히 값을 저장하기 위한 필드   ex) m.username
            //  연관 필드(association field): 연관관계를 위한 필드
            //      단일 값 연관 필드: @ManyToOne, @OneToMany, 대상이 엔티티 ex) m.team
            //      컬렉션 값 연관 필드: @OneToMany, @ManyToMany,대상이 컬렉션 ex) m.orders

            //  경로 표현식 특징
            //      상태 필드(state field): 경로 탐색의 끝, 탐색 X
            //      단일 값 연관 경로: 묵시적 내부 조인(inner join) 발생, 탐색 O
            //      컬렉션 값 연관 경로: 묵시적 내부 조인 발생, 탐색 X
            //          FROM 절에서 명시적 조인을 통해 별칭을 얻으면 별칭을 통해 탐색 가능
            //              ex) select m from Team t join t.members m
            //      명시적 조인: join 키워드를 직접 사용
            //      묵시적 조인: 경로 표현식에 의해 묵시적으로 SQL 조인이 발생 (내부 조인만 가능)
            //  -> 걍 무조건 명시적 조인 써야함
            //  -> 조인은 SQL 튜닝에 중요 포인트
            //  -> 묵시적 조인은 조인이 일어나는 상황을 한눈에 파악하기 어려움

            //JPQL - 페치 조인 (fetch join) (아주 중요함!!!!)
            //  SQL 조인의 종류가 X
            //  JPQL 에서 성능 최적화를 위해 제공하는 기능
            //  연관된 엔티티나 컬렉션을 SQL 한 번에 함께 조회하는 기능
            //  join fetch 명령어 사용

            //  엔티티 페치 조인
            //      회원을 조회하면서 연관된 팀도 함께 조회(SQL 한번에)
            //      SQL 을 보면 회원 뿐만 아니라 팀(T.*)도 함께 select
            //      [JPQL]  select m from Member m join fetch m.team
            //      [SQL]   select m.*, t.* from Member m inner join team t on m.team_id = t.id
//            Team teamA = new Team();
//            teamA.setName("TeamA");
//            em.persist(teamA);
//
//            Team teamB = new Team();
//            teamB.setName("TeamB");
//            em.persist(teamB);
//
//            Member memberA = new Member();
//            memberA.setUsername("MemberA");
//            memberA.setTeam(teamA);
//            em.persist(memberA);
//
//            Member memberB = new Member();
//            memberB.setUsername("MemberB");
//            memberB.setTeam(teamA);
//            em.persist(memberB);
//
//            Member memberC = new Member();
//            memberC.setUsername("MemberC");
//            memberC.setTeam(teamB);
//            em.persist(memberC);
//
//            em.flush();
//            em.clear();
//
//            //엔티티 패치 조인
//            String queryEntity = "select m from Member m join fetch m.team";
//            //컬렉션 페치 조인//distinct 로 엔티티 중복 제거
//            String queryCollection = "select distinct t from Team t join fetch t.members";
//            List<Member> resultListEntity = em.createQuery(queryEntity, Member.class)
//                    .getResultList();
//            List<Team> resultListCollection = em.createQuery(queryCollection, Team.class)
//                    .getResultList();
//
//            for (Member member : resultListEntity) {
//                System.out.println("member = " + member.getUsername() + ", teamName = " + member.getTeam().getName());
//                //MemberA, TeamA(SQL)
//                //MemberB, TeamA(1차 캐시)
//                //MemberC, TeamB(SQL)
//                //하지만 페치 조인을 쓰면 한번에 가능
//            }
//            for (Team team : resultListCollection) {
//                System.out.println("team = " + team.getName() + ", members" + team.getMembers().size());
//                for (Member member : team.getMembers()) {
//                    System.out.println("member = " + member);
//                }
//            }

            //페치 조인과 일반 조인의 차이 -> 일반 조인 실행시 연관된 엔티티를 함께 조회하지 않음
            //  JPQL 은 결과를 반환할 때 연관관계 고려 X
            //  단지 SELECT 절에 지정한 엔티티만 조회할 뿐
            //  여기서는 팀 엔티티만 조회하고, 회원 엔티티는 조회 X
            //  페치 조인을 사용할 때만 연관된 엔티티도 함께 조회(즉시 로딩)
            //  페치 조인은 객체 그래프를 SQL 한번에 조회하는 개념

            //페치 조인의 한계
            //  페치 조인 대상에는 별칭을 줄 수 없다. -> 하이버네이트는 가능, 가급적 사용 X
            //  둘 이상의 컬렉션은 페치 조인 할 수 없다.
            //  컬렉션을 페치 조인하면 페이징 API(setFirstResult, setMaxResults)를 사용할 수 없다.
            //      일대일, 다대일 같은 단일 값 연관 필드들은 페치 조인해도 페이징 가능
            //      하이버네이트는 경고 로그를 남기고 메모리에서 페이징(매우 위험)

            //페치 조인의 특징
            //  연관된 엔티티들을 SQL 한 번으로 조회 - 성능 최적화
            //  엔티티에 직접 적용하는 글로벌 로딩 전략보다 우선함 -> @OneToMany(fetch = FetchType.LAZY) // 글로벌 로딩 전략
            //  실무에서 글로벌 로딩 전략은 모두 지연 로딩
            //  최적화가 필요한 곳은 페치 조인 적용

            //페치 조인 정리
            //  모든 것을 페치 조인으로 해결할 수는 없음
            //  페치 조인은 객체 그래프를 유지할 때 사용하면 효과적
            //  여러 테이블을 조인해서 엔티티가 가진 모양이 아닌 전혀 다른 결과를 내야 하면,
            //  페치 조인보다는 일반 조인을 사용하고 필요한 데이터들만 조회해서 DTO 로 반환하는 것이 효과적

            //JPQL 다형성 쿼리
            //  TYPE -> 조회 대상을 특정 자식을 한정
            //  ex) Item 중에 Book, Movie 를 조회해라 -> select i from Item i where type(i) IN (Book, Movie)
            //  TREAT
            //      자바의 타입 캐스팅과 유사
            //      상속 구조에서 부모 타입을 특정 자식 타입을 다룰 때 사용
            //      FROM, WHERE, SELECT(하이버네이트 지원) 사용

            //JPQL - 엔티티 직접 사용 (기본 키, 외래 키)
            //  JPQL 에서 엔티티를 직접 사용하면 SQL 에서 해당 엔티티의 기본 키 값을 사용

            //  엔티티를 파라미터로 전달
//            String queryParam = "select m from Member m where m = :member";
//            List resultList = em.createQuery(queryParam)
//                    .setParameter("member", member)
//                    .getResultList();
            //  식별자를 직접 전달
//            String query = "select m from Member m where m.id = :memberId";
//            List resultList = em.createQuery(query)
//                    .setParameter("memberId", memberId)
//                    .getResultList();

            //JPQL - Named 쿼리
            //  미리 정의해서 이름을 부여해두고 사용하는 JPQL
            //  정적 쿼리
            //  어노테이션, XML 에 정의
            //  애플리케이션 로딩 시점에 초기화 후 재사용
            //  애플리케이션 로딩 시점에 쿼리를 검증

            //JPQL - 벌크 연산
            //  재고가 10개 미만인 모든 상품의 가격을 10% 상승하려면?
            //  JPA 변경 감지 기능으로 실행하려면 너무 많은 SQL 실행
            //      1. 재고가 10개 미만인 상품을 리스트로 조회한다.
            //      2. 상품 엔티티의 가격을 10% 증가한다.
            //      3. 트랜잭션 커밋 시점에 변경감지가 동작한다.
            //  변경된 데이터가 100건이라면 100번의 UPDATE SQL 실행

            //  벌크 연산 예제
            //      쿼리 한 번으로 여러 테이블 로우 변경(엔티티)
            //      executeUpdate()의 결과는 영향받은 엔티티 수 반환
            //      UPDATE, DELETE 지원
            //      INSERT(insert into .. select, 하이버네이트 지원)
//            int resultCount = em.createQuery("update Member m set m.age = 20")
//                    .executeUpdate();
            //  벌크 연산 주의
            //      벌크 연산은 영속성 컨텍스트를 무시하고 DB 에 직접 쿼리
            //          벌크 연산을 먼저 실행 -> 벌크 연산 수행 후 영속성 컨텍스트 초기화

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
