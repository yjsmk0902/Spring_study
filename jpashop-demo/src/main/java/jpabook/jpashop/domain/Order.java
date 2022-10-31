package jpabook.jpashop.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="ORDERS")
public class Order extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "ORDER_ID")
    private Long id;

    @ManyToOne
    //@ManyToOne
    //  optional => false 로 설정하면 연관된 엔티티가 항상 있어야 한다.
    //  fetch => 글로벌 패치 전략을 설정한다.
    //  cascade => 영속성 전이 기능을 사용한다.
    //  targetEntity => 연관된 엔티티 타입 정보를 설정한다. (요즘엔 사용 안한다고 봐도 됨)
    //  놀라운 사실! 다대일은 mappedBy 속성이 없음 -> 항상 연관관계의 주인이 되어야함
    @JoinColumn(name = "MEMBER_ID")
    //@JoinColumn
    //  외래 키를 매핑할 때 사용
    //  name => 매핑할 외래 키 이름
    //  referencedColumnName => 외래 키가 참조하는 대상 테이블의 컬럼명
    //  foreignKey(DDL) => 외래 키 제약조건을 직접 지정 가능 (테이블 생성 시에만 사용)
    //  unique, nullable insertable, updatable, columnDefinition, table
    //  위의 속성들은 @Column 의 속성과 같음
    private Member member;

    @OneToOne
    @JoinColumn(name = "DELIVERY_ID")
    private Delivery delivery;

    @OneToMany(mappedBy = "order")
    //@OneToMany
    //  mappedBy => 연관관계의 주인 필드를 선택한다.
    //  fetch => 글로벌 페치 전략을 설정한다.
    //  cascade => 영속성 전이 기능을 사용한다.
    //  targetEntity => 연관된 엔티티의 타입 정보를 설정한다. (다대일과 마찬가지로 사용하지 않는다.)
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
}
