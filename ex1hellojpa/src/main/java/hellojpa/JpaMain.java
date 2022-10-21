package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

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
//            em.detach(member); //-> JPA에서 관리하지 않음 (준영속 상태로 전환)


            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }finally {
            em.close();
            emf.close();
        }

    }
}
