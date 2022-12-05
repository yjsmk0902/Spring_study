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
}
