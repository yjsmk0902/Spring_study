package hellojpa;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Member {

    @Id
    @GeneratedValue
    @Column(name="MEMBER_ID")
    private Long id;

    //기본 키 매핑 (Primary Key Mapping)
    //직접 할당 -> @Id만 사용
    //[@GeneratedValue Option]
    //IDENTITY  => DB에 위임, MYSQL
    //SEQUENCE  => DB 시퀀스 오브젝트 사용, ORACLE ->        @SequenceGenerator 필요
    //TABLE     => 키 생성용 테이블 사용, 모든 DB에서 사용 ->      @TableGenerator 필요
    //AUTO      => 방언에 따라 자동 지정, 기본값
    @Column(name="USERNAME")
    private String username;

    //[@Column Option]
    //name => 필드와 매핑할 테이블의 이름
    //insertable, updatable => 등록, 변경 가능 여부
    //nullable(DDL) => null 값의 허용 여부를 설정한다. false로 설정하면 DDL 생성 시에 not null 제약조건이 붙는다.
    //unique(DDL) => @Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약조건을 걸 때 사용한다. (잘 안씀)
    //columnDefinition(DDL) => DB 컬럼 정보를 직접 줄 수 있다.
    //ex -> "varchar(100) default 'EMPTY'"
    //length(DDL) => 문자 길이 제약조건, String 타입에만 사용한다.
    //precision, scale(DDL) =>  BigDecimal 타입에서 사용한다. (BigInteger도 사용 가능)
    //                          precision은 소수점을 포함한 전체 자릿수를, scale은 소수의 자릿수이다. (double, float은 적용 불가능)

//    @Column(name = "TEAM_ID")
//    private Long teamId;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;

    @OneToOne
    @JoinColumn(name = "LOCKER_ID")
    private Locker locker;

//    @ManyToMany
//    @JoinTable(name = "MEMBER_PRODUCT")
//    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<MemberProduct> memberProducts = new ArrayList<>();

    private Integer age;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    //[@Enumerated Option]
    //value =>  EnumType.ORDINAL: enum 순서를 DB에 저장 (사용하지 말자)
    //          EnumType.STRING: enum 이름을 DB에 저장  (필수 조건)

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    //[@Temporal Option]
    //LocalDate, LocalDateTime을 사용할 때는 생략 가능
    //value =>  TemporalType.Date       : 날짜, DB date 타입과 매핑 (2013-10-11)
    //          TemporalType.TIME       : 시간, DB time 타입과 매핑 (11:11:11)
    //          TemporalType.TIMESTAMP  : 날짜와 시간, DB timestamp 타입과 매핑 (2013-10-11 11:11:11)

    @Lob
    private String description;

    //[@Lob]
    //@Lob은 지정할 수 있는 속성이 없음
    //매핑하는 필드 타입이 문자면 CLOB 매핑, 나머지는 BLOB 매핑
    //CLOB => String, char[], java.sql.CLOB
    //BLOB => byte[], java.sql.BLOB

    @Transient
    private int temp;

    //@Trasient
    //필드 매핑X
    //DB 저장X, 조회X
    //주로 메모리상에서만 임시로 어떤 값을 보관하고 싶을 때 사용

    //매핑 어노테이션 정리
    //@Column => 컬럼 매핑
    //@Temporal => 날짜 타입 매핑
    //@Enumerated => enum 타입 매핑
    //@Lob => BLOB, CLOB 매핑 (저장 타입이 굉장히 큰 컨텐츠 일때)
    //@Transient => 특정 필드를 컬럼에 매핑하지 않음 (매핑 무시)
    public Member() {
    }

    public Member(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

//    public Long getTeamId() {
//        return teamId;
//    }
//
//    public void setTeamId(Long teamId) {
//        this.teamId = teamId;
//    }
    public Team getTeam() {
        return team;
    }

    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);    //연관관계 편의 메소드를 생성
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }
}
