package study.datajpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDTO;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    //도메인 클래스 컨버터 사용 전
    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    //도메인 클래스 컨버터 사용 시 (트랜잭션이 없는 범위에서 조회했기 때문에 단순 조회용으로만 사용해야 한다.)
    //걍 권장하지 않음
    @GetMapping("/members/{id}")
    public String findMember2(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    //Web 확장 - 페이징과 정렬
    //  파라마터로 Pageable 을 받아 사용한다.
    //  요청 파라미터
    //      ex) /members?page=0&size=3&sort=id,desc&sort=username,desc
    //      page: 현재 페이지, 0부터 시작한다.
    //      size: 한 페이지에 노출할 데이터 건수
    //      sort: 정렬 조건을 정의한다. , 를 기준으로 파라미터 추가 가능
    //  기본값은 글로벌 설정에서 설정가능 application.yml
    //  @PageableDefault(page =..., size =..., sort = ...) 를 통해 개별 페이징 설정 가능

    //  접두사 - 페이징 정보가 둘 이상이면 접두사로 구분
    //      @Qualifier 에 접두사명 추가 "{접두사명}_xxx"
    //      public String list(@Qualifier("member") Pageable memberPageable, @Qualifier("order") Pageable orderPageable,...)

    //  Page 내용을 DTO 로 변환하기
    //      엔티티를 API 로 노출하면 다양한 문제가 발생한다. 따라서 반드시 DTO 로 변환해서 반환해야 한다.
    //      Page 는 map() 을 지원해서 내부 데이터를 다른 것을 변경할 수 있다.

    //  Page 를 1부터 시작하기
    //      스프링 데이터는 Page 를 0부터 시작한다.
    //      만약 1부터 시작하려면?
    //          1. Pageable, Page 를 파라미터와 응답 값으로 사용하지 않고, 직접 클래스를 만들어서 처리한다.
    //              그리고 직접 PageRequest(Pageable 구현체)를 생성해서 리포지토리에 넘긴다.
    //              물론 응답값도 Page 대신에 직접 만들어서 제공해야 한다.
    //          2. spring.data.web.pageable.one-indexed-parameters 를 true 로 설정한다.
    //              하지만 이 방법은 web 에서 page 파라미터를 -1 처리 할 뿐이다. -> 한계가 있다.
    //          3. 걍 0부터 써라. 그게 아니면 대안을 한 번 찾아보자.
//    @GetMapping("/members")
//    public Page<Member> list(@PageableDefault(size = 5) Pageable pageable) {
//        return memberRepository.findAll(pageable);
//    }
    @GetMapping("/members")
    public Page<MemberDTO> list(@PageableDefault(size = 5) Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(1, 2);
        return memberRepository.findAll(pageable).map(MemberDTO::new);
    }

    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            memberRepository.save(new Member("user" + i, i));
        }
    }
}
