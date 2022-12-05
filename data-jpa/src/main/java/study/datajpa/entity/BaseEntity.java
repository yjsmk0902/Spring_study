package study.datajpa.entity;

import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;


//스프링 데이터 JPA 를 이용한 Auditing
//  스프링 부트 설정 클래스에 @EnableJpaAuditing 을 적용하는 거 잊지말자!!
//  해당 엔티티에는 @EntityListeners(AuditingEntityListener.class) 적용
//  4가지의 요소 중 필요한 것만 쓰고 싶을 때는 클래스를 분리하여 상속받아 사용할 수도 있다.
//      ex) BaseEntity -> BaseTimeEntity, BaseByEntity
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    //등록일
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    //수정일
    @LastModifiedDate
    private LocalDateTime updatedDate;

    //등록자
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    //수정자
    @LastModifiedBy
    private String updatedBy;
}
