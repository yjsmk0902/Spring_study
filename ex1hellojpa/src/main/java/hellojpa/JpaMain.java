package hellojpa;

import hellojpa.inheritance.Movie;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {

        //EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        //transaction
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
//            //JPQL
//            List<Member> result = em.createQuery("select m from Member as m", Member.class)
//                    .getResultList();

//            //1차 캐시에서 조회하는 경우
//            //비영속
//            Member member = new Member();
//            member.setId(100L);
//            member.setName("HelloJPA");
//
//            //영속
//            em.persist(member);
//
//            //조회 => 쿼리를 불러오지 않고 캐시에 저장된 값을 찾아온다.
//            Member findMember = em.find(Member.class, 100L);

//            //DB에서 조회

//            1차 캐시로 반복 가능한 읽기(REPEATABLE READ) 등급의 트랜잭션 격리 수준을
//                    DB가 아닌 애플리케이션 차원에서 제공

//            //만약 DB에 저장되어 있는 경우 => 쿼리가 한번만 불러와짐
//            Member findMember1 = em.find(Member.class, 1L);
//            Member findMember2 = em.find(Member.class, 1L);
//            findMember1==findMember2 가 성립한다. => 영속 엔티티의 동일성 보장

//            //트랜잭션을 지원하는 쓰기 지연 (버퍼링 => 모아서 DB에 넣는다. 옵션 추가 가능)
//            Member member1 = new Member(150L, "A");
//            Member member2 = new Member(151L, "B");
//
//            em.persist(member1);    //쓰기 지연 SQL 저장소에 저장
//            em.persist(member2);    //쓰기 지연 SQL 저장소에 저장
//            // => 트랜잭션 커밋 시에 순차적으로 한꺼번에 커밋

//            //엔티티 수정 (변경 감지 => Dirty Checking)
//            Member member = em.find(Member.class, 1L);
//            member.setName("abcd");
//            //값의 변경을 감지시에 update 쿼리가 날아감
//            //Entity와 Snapshot을 비교하여 DB에 반영

//            //엔티티 삭제
//            Member member = em.find(Member.class, 1L);
//            em.remove(member);

            //영속성 컨텍스트를 플러시 하는 방법
            //em.flush() -> 직접 호출
            //tx.commit() -> 플러시 자동 호출
            //JPQL 쿼리 실행 -> 플러시 자동 호출
            //플러시 -> 영속성 컨텍스트를 비우지 않음 + 변경내용을 DB에 동기화

            //준영속 상태로 만드는 방법
            //em.detach(entity) -> 특정 엔티티만 준영속 상태로 전환
            //em.clear() -> 영속성 컨텍스트를 완전히 초기화
            //em.close() -> 영속성 컨텍스트를 종료

//            //준영속 상태의 예시
//            Member member = em.find(Member.class, 1L);
//            member.setName("AAA");
//            em.detach(member); //-> JPA 에서 관리하지 않음 (준영속 상태로 전환)

//            //객체를 테이블에 맞추어 모델링 (식별자로 다시 조회, 객체 지향적인 방법은 아니다.)
//            Team team = new Team();
//            team.setName("TeamA");
//            em.persist(team);
//
//            Member member = new Member();
//            member.setUsername("member1");
//            member.setTeamId(team.getId());
//            em.persist(member);
//
//            Member findMember = em.find(Member.class, member.getId());
//
//            Long findTeamId = findMember.getTeamId();
//            Team findTeam = em.find(Team.class, findTeamId);
//
//            //객체를 테이블에 맞추어 데이터 중심으로 모델링하면, 협력 관계를 만들 수 없다.

//            //객체 지향 모델링(객체의 참조와 테이블의 외래 키를 매핑) -> 연관관계의 주인은 무조건 외래 키를 기준
//            Team team = new Team();
//            team.setName("TeamA");
//            em.persist(team);
//
//            Member member = new Member();
//            member.setUsername("memberA");
//            member.setTeam(team);
//            em.persist(member);
//
//            Member findMember = em.find(Member.class, member.getId());
//
//            Team findTeam = findMember.getTeam();
//
//            //양방향 매핑
//            List<Member> members = findTeam.getMembers();

            //양방향 매핑시 가장 많이 하는 실수
            //1. 연관관계의 주인에 값을 입력하지 않는 경우
            //2. 무한 루프를 조심하자 (lombok, toString(), JSON 생성 Lib.) -> 전달 폼(DTO)를 따로 만든다.
            // => 처음엔 무조건 단방향 매핑으로 설계하기
            // + 양방향 매핑은 반대 방향으로 조회 기능이 추가된 것 뿐 (굳이?)
            // + 근데 또 JPQL 사용시에 역방향을 쓸 일이 많음
            // 결론 -> 단방향 매핑을 잘 하고 양방향은 필요할 때만 추가하면 됨
//            Member member = new Member();
//            member.setUsername("memberA");
//
//            Team team = new Team();
//            team.setName("TeamA");
//
//            //team.getMembers().add(member);  //역방향 (주인이 아닌 방향)만 연관관계 설정 -> 틀림
//            member.changeTeam(team);           //정방향 -> 이렇게 해야함 // 정방향, 역방향 양쪽으로 설정해주자
//                                            //캐시를 생각하면 양쪽에 넣어주어야 빈 객체없이 채울 수 있다.
//                                            //연관관계 편의 메서드를 사용하는 것을 권장한다.
//                                            //어느쪽 엔티티에서 연관관계 편의 메서드를 작성해도 상관없다.
//            em.persist(team);
//            em.persist(member);

            //다양한 연관관계 매핑
            //연관관계 매핑시 고려사항 3가지
            //  1. 다중성 / 2. 단방향, 양방향 / 3. 연관관계의 주인
            //  1. 다중성
            //      다대일 : @ManyToOne
            //      일대다 : @OneToMany
            //      일대일 : @OneToOne
            //      다대다 : @ManyToMany
            //  2. 단방향, 양방향
            //      테이블 - 외래 키 하나로 양쪽 조인 가능
            //           - 방향이라는 개념이 없음
            //      객체 - 참조용 필드가 있는 쪽으로만 참조 가능
            //          - 한쪽만 참고하면 단방향
            //          - 양쪽이 서로 참조하면 양방향
            //  3. 연관관계의 주인
            //      테이블은 외래 키 하나로 두 테이블이 연관관계를 맺음
            //      객체 양방향 관계는 A->B / B->A 처럼 참조가 2군데인 것을 의미
            //      객체 양방향 관계는 둘 중 테이블의 외래 키를 관리할 곳을 지정해야함
            //      연관관계의 주인 : 외래 키를 관리하는 참조
            //      주인의 반대편 : 외래 키에 아무 영향을 주지 않음, 단순 조회만 가능

            //다대일 단방향
            //  가장 많이 사용하는 연관관계
            //  다대일의 반대는 일대다

            //다대일 양방향
            //  외래 키가 있는 쪽이 연관관계의 주인
            //  양쪽을 서로 참조하도록 개발

            //일대다 단방향 (권장하지 않음)
            //  일대다 단방향은 일대다(1:N)에서 1이 연관관계의 주인
            //  테이블 일대다 관계는 항상 N쪽에 외래 키가 있음 -> 보통은 외래 키를 가진쪽이 연관관계의 주인
            //  객체와 테이블의 차이 때문에 반대편 테이블의 외래 키를 관리하는 특이한 구조
            //  @JoinColumn 을 꼭 사용해야 함. 그렇지 않으면 조인 테이블 방식을 사용함 (중간에 테이블을 하나 추가하는 방식)
            //  단점 - 외래 키를 관리하는 쪽과 연관관계의 주인이 서로 다른 테이블에 있음
            //      - 연관관계 관리를 위해 추가로 UPDATE SQL 을 실행해야함
            //  이거 쓸바에는 그냥 다대일 양방향 쓰삼

            //일대다 양방향
            //  이런 매핑은 걍 공식적으로 존재조차 안함
            //  @JoinColumn(insertable=false, updatable=false)
            //  읽기 전용 필드를 사용해서 양방향처럼 사용하는 방법이 있기는 함
            //  근데 굳이 왜그렇게함 걍 다대일 양방향 쓰삼

            //일대일 단방향
            //  일대일 관계는 그 반대도 일대일
            //  주 테이블이나 대상 테이블 중에 외래키 선택 가능
            //  외래 키에 DB 유니크 제약조건 추가 (UNI)
            //  다대일 단방향 매핑과 유사

            //일대일 양방향
            //  다대일 양방향 매핑처럼 외래 키가 있는 곳이 연관관계의 주인
            //  반대편은 mappedBy 적용

            //일대일 : 대상 테이블에 외래 키 단방향 -> JPA 가 지원하지 않음

            //일대일 : 대상 테이블에 외래 키 양방향
            //  일대일 주 테이블에 외래 키 양방향과 매핑 방법은 같음

            //일대일 관계는 좀 그때마다 잘 생각해서 해야됨

            //다대다 -> 쓰지 마라 걍
            //  관계형 DB는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없음
            //  연결 테이블을 추가해서 일대다, 다대일 관계로 풀어내야함
            //  객체는 컬렉션을 사용해서 객체 2개로 다대다 관계 가능
            //  편리해 보이지만 실무에서 사용 X
            //  연결 테이블이 단순히 연결만 하고 끝나지 않음
            //  주문시간, 수량 같은 데이터가 들어올 수 있음

            //다대다 한계 극복
            //  연결 테이블용 엔티티 추가 (연결 테이블을 엔티티로 승격)
            //  @ManyToMany => @OneToMany + @ManyToOne
            //  연결 테이블의 PK는 유연하게 따로 설정 <-> 두개의 FK를 묶어서 PK로 쓰는 방법도 있음

            //고오급 매핑 - 상속관계 매핑
            //  @주요 어노테이션
            //      @Inheritance(strategy = Inheritance.XXX) => 부모 엔티티에 써준다. 전략에 따라 XXX 가 바뀜
            //      @DiscriminatorColumn => 부모 엔티티에 써준다. 타입 종류 이름 지정 가능. 디폴트는 name = "DTYPE"
            //      @DiscriminatorValue("XXX") => 자식 엔티티에 써준다.

            //  1. 조인 전략 -> 데이터를 각각 테이블로 나누고 구성할 때 조인으로 가져오는거
            //      InheritanceType.JOINED 옵션 선택하기
            //      장점 : 테이블 정규화 / 외래 키 참조 무결성 제약조건 활용가능 / 저장공간 효율화
            //      단점 : 조회시 조인을 많이 사용하여 성능 저하 / 조회 쿼리 복잡 / 데이터 저장시 INSERT SQL 2번 호출

            //  2. 단일 테이블 전략 -> 논리 모델을 그냥 한 테이블로 다 합쳐버리는거 (다 때려박기)
            //      InheritanceType.SINGLE_TABLE 옵션 선택하기 (DTYPE 생략 가능하지만 있는게 운영상 좋음)
            //      장점 : 조인이 필요없으므로 성능 빠름 / 조회 쿼리 단순
            //      단점 : 자식 엔티티 매핑 컬럼은 모두 null 을 허용해버림 / 테이블이 크고 복잡해질 수 있음

            //  3. 구현클래스마다 테이블 전략 -> 각 테이블 별로 다 정보를 구현해버리기      => DB 설계자와 ORM 전문가가 비추함
            //      InheritanceType.TABLE_PER_CLASS 옵션 선택하기
            //      장점 : 서브 타입을 명확하게 구분하여 처리할 때 효과적 / not null 사용 가능
            //      단점 : 여러 자식 테이블을 한꺼번에 조회할 때 성능 느림 / 자식들을 통합해서 쿼리하기 어려움

//            Movie movie = new Movie();
//            movie.setDirector("directorA");
//            movie.setActor("actorA");
//            movie.setName("영화앙");
//            movie.setPrice(15000);
//
//            em.persist(movie);
//
//            em.flush();
//            em.clear();
//
//            Movie findMovie = em.find(Movie.class, movie.getId());
//            System.out.println("findMovie = " + findMovie);
//
            //      자세한내용은 PDF 에 있는 그림 잘 확인해보기

            //@MappedSuperClass => 공통 매핑 정보가 필요할 때 사용 (ex. id, name 뭐 이런거)
            //  상속관계 매핑X (헷갈리지 말자 진짜)
            //  엔티티X, 테이블과 매핑X
            //  부모 클래스를 상속 받는 자식 클래스에 매핑 정보만 제공
            //  조회, 검색 불가(em.find(BaseEntity) 불가)
            //  직접 생성해서 사용할 일이 없으므로 추상 클래스 권장
            //  해당 클래스에 @Colum 을 사용하여 이름도 매핑 가능
//            Member member = new Member();
//            member.setUsername("userA");
//            member.setCreatedBy("Kim");
//            member.setCreatedDate(LocalDateTime.now());
//
//            em.persist(member);
//            em.flush();
//            em.clear();

            //프록시와 연관관계
            //  프록시의 특징
            //      실제 클래스를 상속 받아서 만들어짐
            //      실제 클래스와 겉모양이 같다.
            //      사용하는 입장에서는 진짜인지 프록시인지 구분하지 않아도 된다. (이론상은 그러함)
            //      프록시 객체는 실제 객체의 참조를 보관
            //      프록시 객체를 호출하면 프록시 객체는 실제 객체의 메소드 호출
            //      --------------------------------------------
            //      프록시 객체는 처음 사용할 때 한 번만 초기화
            //      프록시 객체를 초기화할 때, 프록시 객체가 실제 엔티티로 바뀌는 건 아님
            //      초기화되면 프로시 객체를 통해서 실제 엔티티에 접근 가능
            //      프록시 객체는 원본 엔티티를 상속받음, 따라서 타입 체크시 주의해야함(== 대신 instance of 를 사용해야함)
            //      영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 호출해도 실제 엔티티가 반환
            //      영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태(em.close() / em.detach(객체))일 때, 프록시를 초기화하면 문제 발생
            //  프록시 확인
            //      프록시 인스턴스의 초기화 여부 확인
            //          emf.getPersistenceUnitUtil.isLoaded(Object entity)
            //      프록시 클래스 확인 방법
            //          entity.getClass().getName() -> 걍 무식하게 찍어야됨
            //      프록시 강제 초기화
            //          org.hibernate.Hibernate.initialize(entity)
            //          -> entity.getUsername() 같은걸로 강제 호출로 초기화도 되긴 하지만 위처럼 강제 초기화도 가능
            //  em.find() vs em.getReference()
            //      em.find() -> DB를 통해서 실제 엔티티 객체를 조회
            //      em.getReference() -> DB 조회를 미루는 가짜(프록시) 엔티티 객체를 조회
            //      근데 거의 안쓰긴 함 => 즉시 로딩과 지연 로딩 배울려고 하는거임

            //즉시 로딩과 지연로딩(Fetch)
            //  @ManyToOne(fetch = FetchType.LAZY) => 지연로딩
            //  @ManyToOne(fetch = FetchType.EAGER) => 즉시로딩

            //Team 을 자주 사용하지 않을 경우, 조회할 때 굳이 Team 까지 조인할 필요가 없다.
            //따라서 지연로딩으로 성능을 최적화한다.
//            Team team = new Team();     //Team 의 fetch 가 LAZY 로 되어있음
//            team.setName("TeamA");
//            em.persist(team);
//
//            Member member = new Member();
//            member.setUsername("MemberA");
//            member.setTeam(team);
//            em.persist(member);
//
//            em.flush();
//            em.clear();
//
//            Member findMember = em.find(Member.class, member.getId());      //-> 이때까지는 초기화가 안되어있음
//            findMember.getTeam().getName(); //-> Team 의 값을 실제로 사용하는 시점에 프록시 초기화
            //실무에서는 절대로 즉시로딩 쓰지 않기!
            //     => 즉시 로딩을 적용하면 예상하지 못한 SQL 이 발생
            //        즉시 로딩은 JPQL 에서 N+1 문제를 일으킴
            //        @ManyToOne, @OneToOne 은 기본이 즉시 로딩 -> LAZY 로 전부 설정해줘야함
            //        @OneToMany 는 기본이 지연 로딩
            //가이드에서도 가급적 지연로딩을 사용하라 권함.

//            Team teamA = new Team();
//            Team teamB = new Team();
//            teamA.setName("TeamA");
//            teamB.setName("TeamB");
//
//            em.persist(teamA);
//            em.persist(teamB);
//
//            Member memberA = new Member();
//            Member memberB = new Member();
//            memberA.setUsername("MemberA");
//            memberB.setUsername("MemberB");
//            memberA.setTeam(teamA);
//            memberB.setTeam(teamB);
//
//            em.persist(memberA);
//            em.persist(memberB);
//
//            em.flush();
//            em.clear();
//            List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();

            //SQL: select * from Member
            //SQL: select * from Team where TEAM_ID = xxx -> SQL 이 하나 더날아감 (즉시 로딩이기 때문 (N+1 prob.))
            //따라서 LAZY로 설정해야함
            //JPQL 의 etch join 으로 해결 가능 나중에 배울거임

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }finally {
            em.close();
            emf.close();
        }

    }
}
