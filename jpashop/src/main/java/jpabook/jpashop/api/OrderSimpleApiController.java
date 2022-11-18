package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


//xToOne (ManyToOne, OneToOne)
//  Order
//  Order -> Member
//  Order -> Delivery
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    //V1. 엔티티 직접 노출
    //  Hibernate5Module 모듈 등록, LAZY = null 처리
    //  양방향 관계 문제 발생 -> @JsonIgnore
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByCriteria(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();        //LAZY 강제 초기화
            order.getDelivery().getAddress();   //LAZY 강제 초기화
        }
        return all;
    }

    @Data
    private static class SimpleOrderDTO {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDTO(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();         //LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY 초기화
        }
    }

    //V2. 엔티티를 조회해서 DTO 로 변환 (fetch join 사용X)
    //  단점: 지연로딩으로 쿼리 N 번 호출
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDTO> ordersV2() {
        List<Order> orders = orderRepository.findAllByCriteria(new OrderSearch());
        return orders.stream()
                .map(SimpleOrderDTO::new)
                .collect(Collectors.toList());
    }

    //V3. 엔티티를 조회해서 DTO 로 변환 (fetch join 사용)
    //  fetch join 으로 쿼리 1번 호출
    //  참고: fetch join 에 대한 자세한 내용은 JPA 기본편 참고 (진짜 중요!!)
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDTO> ordersv3() {
        return orderRepository.findAllWithMemberDelivery().stream()
                .map(SimpleOrderDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDTO> ordersV4() {
        return orderRepository.findOrderDTOs();
    }
}
