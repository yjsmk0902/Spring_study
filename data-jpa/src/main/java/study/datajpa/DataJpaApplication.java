package study.datajpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@SpringBootApplication
@EnableJpaAuditing//(modifyOnCreate = false)로 저장데이터만 입력하고 싶을 때 쓸 수 있다.
public class DataJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataJpaApplication.class, args);
	}

	//스프링 데이터 JPA Auditing
	//	AuditorAware 스프링 빈 등록
	//	실무에서는 세션 정보나, 스프링 시큐리니 로그인 정보에서 ID 를 받음
	@Bean
	public AuditorAware<String> auditorProvider() {
		return () -> Optional.of(UUID.randomUUID().toString());
	}

	//스프링 데이터 JPA 분석
	//	스프링 데이터 JPA 구현체 분석
	//		@Repository 적용: JPA 예외를 스프링이 추상화한 예외로 변환
	//		@Transactional 트랜잭션 적용:
	//			JPA 의 모든 변경은 트랜잭션 안에서 동작
	//			스프링 데이터 JPA 는 변경(등록, 수정, 삭제) 메서드를 트랜잭션 처리
	//			서비스 계층에서 트랜잭션을 시작하지 않으면 리퍼지토리에서 트랜잭션 시작
	//			서비스 계층에서 트랜잭션을 시작하면 리퍼지토리는 해당 트랜잭션을 전파 받아서 사용
	//			따라서 스프링 데이터 JPA 를 사용할 때 트랜잭션이 없어도 데이터 등록, 변경이 가능했음
	//			+사실은 트랜잭션이 리포지토리 계층에 걸려있는 것임
	//		@Transactional(readOnly = true)
	//			데이터를 단순히 조회만 하고 변경하지 않는 트랜잭션에서 readOnly = true 옵션을 사용하면 플러시를 생략해서 약간의 성능 향상을 얻을 수 있음
	//			자세한 내용은 JPA 책 15.4.2 읽기 전용 쿼리의 성능 최적화 참고
}
