package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class OrderApiController {

    private final OrderRepository orderRepository;

    //V1. 엔티티 직접 노출
    //  엔티티가 변하면 API 스펙이 변한다.
    //  트랜잭션 안에서 지연 로딩 필요
    //  양방향 연관관계 문제
    //  orderItem, item 관계를 직접 초기화하면 Hibernate5Module 설정에 의해 엔티티를 JSON 으로 생성한다.
    //  양방향 연관관계면 무한 루프에 걸리지 않게 한 곳에 @JsonIgnore 를 추가해야 한다.
    //  엔티티를 직접 노출하므로 좋은 방법은 아니다
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByCriteria(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();                        //LAZY 강제 초기화
            order.getDelivery().getAddress();                   //LAZY 강제 초기화
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.forEach(o->o.getItem().getName());       //LAZY 강제 초기화
        }
        return all;
    }

    //V2. 엔티티를 조회해서 DTO 로 변환 (fetch join 사용X)
    //  트랜잭션 안에서 지연 로딩 필요
    //  지연 로딩으로 너무 많은 SQL 실행
    //  SQL 실행수
    //      order 1번
    //      member, address N 번(order 조회 수 만큼)
    //      orderItem N 번(order 조회 수 만큼)
    //      item N번(orderItem 조회 수 만큼)
    //  LAZY 로딩은 영속성 컨텍스트에 있으면 영속성 컨텍스트에 있는 엔티티를 사용하고,
    //  없으면 SQL 을 실행한다.
    @GetMapping("/api/v2/orders")
    public List<OrderDTO> ordersV2(){
        List<Order> orders = orderRepository.findAllByCriteria(new OrderSearch());
        List<OrderDTO> collect = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());

        return collect;
    }

    //V3. 엔티티를 DTO 로 변환 (fetch join 사용)
    //  페이징 시에는 N 부분을 포기해야함 (대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경 가능)
    //  페치 조인으로 SQL 이 1번만 실행됨
    //  distinct 를 사용한 이유는 1대다 조인이 있으므로 DB row 가 증가한다. 그 결과 같은 order 엔티티의 조회 수도 증가한다.
    //  JPA 의 distinct 는 SQL 에 distinct 를 추가하고, 더해서 같은 엔티티가 조회되면, 애플리게이션 중복을 걸러준다.
    //  이 예시에서 order 가 컬렉션 페치 조인 때문에 중복 조회되는 것을 막아준다.
    //  단점: 페이징이 불가능하다. -> 컬렉션 페치 조인으로는 페이징이 불가능하다.
    //  컬렉션 페치 조인은 1개만 가능하다. 컬렉션에 둘 이상의 페치 조인을 사용하면 안된다. (데이터 부정합이 일어날 수 있음)
    @GetMapping("/api/v3/orders")
    public List<OrderDTO> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        return orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
    }

    //V3.1. 엔티티를 조회해서 DTO 로 변환 / 페이징 고려
    //  ToOne 관계만 우선 모두 페치 조인으로 최적화
    //  컬렉션 관계는 hibernate.default_batch_fetch_size, @BatchSize 로 최적화
    //  장점
    //      쿼리 호출 수가 1+N -> 1+1로 최적화된다.
    //      조인보다 DB 데이터 전송량이 최적화된다. (Order 과 OrderItem 을 조인하면 Order 가 OrderItem 만큼 중복해서 조회된다.
    //      이 방식은 각각 조회하는 방식이므로 전송해야 할 중복 데이터가 없다.)
    //      페치 조인 방식과 비교해서 쿼리 호출 수가 약간 증가하지만, DB 데이터 전송량이 감소한다.
    //      컬렉션 페치 조인은 페이징이 불가능하지만 이 방법은 페이징이 가능하다.
    //  결론 -> ToOne 관계는 페치 조인해도 페이징에 영향을 주지 않는다. 따라서 ToOne 관계는 페치 조인으로 쿼리 수를 줄여 해결하고,
    //          나머지는 hibernate.default_batch_fetch_size 로 최적화하자.
    //  +default_batch_fetch_size 의 크기는 적당한 사이즈를 골라야 하는데, 100~1000 사이를 선택하는 것을 권장한다.ㄹ
    //      이 전략을 SQL IN 절을 사용하는데, DB 에 따라 IN 절 파라미터를 1000으로 제한하기도 한다.
    //      1000개를 한 번에 불러오므로 순간 부하가 증가할 수 있지만 결국에는 전체 데이터를 조회해야하므로 메모리 사용량은 같다.
    //
    @GetMapping("/api/v3.1/orders")
    public List<OrderDTO> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        return orders.stream().map(OrderDTO::new).collect(Collectors.toList());
    }


    //V4. JPA 에서 DTO 로 바로 조회, 컬렉션 N 조회 (1+N Query)
    //  페이징 가능

    @Data
    static class OrderDTO {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDTO> orderItems;

        public OrderDTO(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDTO::new)
                    .collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDTO{

        private String itemName;    //상품명
        private int orderPrice;     //주문 가격
        private int count;          //주문 수량

        public OrderItemDTO(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}
