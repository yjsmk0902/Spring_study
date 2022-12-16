package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDTO;
import study.querydsl.entity.Member;

import java.util.List;

//사용자 정의 인터페이스 작성
public interface MemberRepositoryCustom {
    List<MemberTeamDTO> search(MemberSearchCondition condition);

    //스프링 데이터 페이징 활용1 - Querydsl 페이징 연동
    Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable);

    Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
