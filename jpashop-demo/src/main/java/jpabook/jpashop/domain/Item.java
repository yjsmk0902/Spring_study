package jpabook.jpashop.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Item {

    @Id
    @GeneratedValue
    @Column(name = "ITEM_ID")
    private Long id;

    //N:M 관계는 1:N / N:1로
    //  테이블의 N:M 관계는 중간 테이블을 이용해서 1:N, N:1
    //  실전에서는 중간 테이블이 단순하지 않다.
    //  @ManyToMany 는 제약: 필드 추가X, 엔티티 테이블 불일치
    //  실전에서는 걍 쓰지 마라 -> 여기서는 그냥 함 해본거임
    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    private String name;

    private int price;

    private int stockQuantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
