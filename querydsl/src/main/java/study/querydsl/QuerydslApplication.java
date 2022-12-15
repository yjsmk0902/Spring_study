package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;

@SpringBootApplication
public class QuerydslApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuerydslApplication.class, args);
	}

	//JPAQueryFactory 스프링 빈 등록
	@Bean
	JPAQueryFactory jpaQueryFactory(EntityManager em) {
		return new JPAQueryFactory(em);
	}
	//	+동시성 문제는 걱정하지 않아도 됨. 엔티티 매니저가 실제 동작 시점에 진짜 엔티티 매니저를 찾아주는 프록시용
	//	가짜 엔티티 매니저이다. 이러한 가짜 엔티티 매니저는 실제 사용 시점에 트랜잭션 단위로 실제 엔티티 매니저를
	//	할당해준다.

}
