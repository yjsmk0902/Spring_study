package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDTO;
import jpabook.jpashop.repository.order.query.OrderItemQueryDTO;
import jpabook.jpashop.repository.order.query.OrderQueryDTO;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
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
    private final OrderQueryRepository orderQueryRepository;

    //API 개발 고급 정리
    //  엔티티 조회
    //      엔티티를 조회해서 그대로 반환: V1
    //      엔티티 조회 후 DTO 로 변환: V2
    //      페치 조인으로 쿼리 수 최적화: V3
    //      컬렉션 페이징과 한계 돌파: V3.1
    //          컬렉션은 페치 조인시 페이징이 불가능
    //          ToOne 관계는 페치 조인으로 쿼리 수 최적화
    //          컬렉션은 페치 조인 대신에 지연 로딩을 유지하고, hibernate.default_batch_fetch_size, @BatchSize 로 최적화
    //  DTO 직접 조회
    //      JPA 에서 DTO 를 직접 조회: V4
    //      컬렉션 조회 최적화 - 일대다 관계인 컬렉션은 IN 절을 활용해서 메모리에 미리 조회해서 최적화: V5
    //      플랫 데이터 최적화 - JOIN 결과를 그대로 조회 후 애플리케이션에서 원하는 모양으로 직접 변환: V6
    //  권장 순서
    //      1. 엔티티 조회 방식으로 우선 접근    (페치 조인이나 batch 를 통해 유연한 성능 최적화 가능)
    //          -페치 조인으로 쿼리 수를 최적화
    //          -컬렉션 최적화
    //              페이징 필요한 경우: hibernate.default_batch_fetch_size, @BatchSize 로 최적화
    //              페이징 필요X: 페치 조인 사용
    //      2. 엔티티 조회 방식으로 해결이 안되면 DTO 조회 방식 사용 (유연한 성능 최적화가 힘듦)
    //      3. DTO 조회 방식으로 해결이 안되면 NativeSQL or 스프링 JdbcTemplate

    //OSIV 와 성능 최적화
    //  Open Session In View
    //  spring.jpa.open-in-view: true 기본값
    //  OSIV 전략은 트랜잭션 시작처럼 최초 DB connection 시작 시점부터 API 응답이 끝날 때까지 영속성 컨텍스트와 DB connection 을 유지한다.
    //      -> 그래서 지금까지 View Template 이나 API 컨트롤러에서 지연 로딩 가능
    //  지연 로딩은 영속성 컨텍스트가 살아있어야 가능하고, 영속성 컨텍스트는 기본적으로 DB connection 을 유지한다.
    //  하지만 긴 시간동안의 DB connection 리소스 사용은 실시간 트래픽이 중요한 애플리케이션에서는 커넥션이 모자랄 수 있다.
    //  OSIV 를 끄면 트랜잭션을 종료할 때 영속성 컨텍스트를 닫고, DB connection 도 반환한다. 따라서 낭비가 없다.
    //  OSIV 를 끄면 모든 지연 로딩을 트랜잭션 안에서 처리해야 한다. -> 지연 로딩 코드를 트랜잭션 안으로 넣어야 함
    //  그리고 View Template 에서 지연 로딩이 동작하지 않는다. -> 트랜잭션이 끝나기 전에 지연 로딩을 강제 호출

    //커맨드와 쿼리 분리
    //  예를 들어, OrderService   -> OrderService: 핵심 비즈니스 로직
    //                          -> OrderQueryService: 화면이나 API 에 맞춘 서비스 (주로 읽기 전용 트랜잭션 사용)

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
    //  ToOne 관계들을 먼저 조회하고, ToMany 관계는 각각 별도로 처리한다.
    //      ToOne 관계는 조인해도 데이터 row 수가 증가하지 않는다.
    //      ToMany 관계는 조인하면 row 수가 증가한다.
    //  row 수가 증가하지 않는 ToOne 관계는 조인으로 최적화하기 쉬우므로 한번에 조회하고,
    //  ToMany 관계는 최적화하기 어려우므로 findOrderItems() 같은 별도의 메소드로 조회한다.
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDTO> ordersV4() {
        return orderQueryRepository.findOrderQueryDTOs();
    }

    //V5. JPA 에서 DTO 로 바로 조회, 컬렉션 조회 최적화 버전(1+1 Query)
    //  페이징 가능
    //  Query: 루트 1번, 컬렉션 1번
    //  ToOne 관계들을 먼저 조회하고, 여기서 얻은 식별자 orderId 로 ToMany 관계인 OrderItem 을 한번에 조회
    //  MAP 을 사용해서 매칭 성능 향상 (O(1))
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDTO> ordersV5() {
        return orderQueryRepository.findAllByDTO_optimization();
    }

    //V6. JPA 에서 DTO 로 바로 조회, 플랫 데이터 (1Query)
    //  페이징 불가능
    //  Query: 1번
    //  단점
    //      쿼리는 한번이지만 조인으로 인해 DB 에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되므로 상황에 따라 V5 보다 더 느릴 수 있다.
    //      애플리케이션에서 추가 작업이 크다
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDTO> ordersV6() {
        List<OrderFlatDTO> flats = orderQueryRepository.findAllByDTO_flat();

        return flats.stream()
                .collect(Collectors.groupingBy(o->new OrderQueryDTO(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        Collectors.mapping(o->new OrderItemQueryDTO(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), Collectors.toList())))
                .entrySet().stream()
                .map(e->new OrderQueryDTO(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue())).collect(Collectors.toList());
    }

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
