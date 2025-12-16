package com.recipe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shopping_list")
public class ShoppingList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ingredient_name", nullable = false, length = 100)
    private String ingredientName; // 食材名称

    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity; // 数量

    @Column(name = "unit", length = 20)
    private String unit; // 单位

    @Column(name = "purchased")
    private Boolean purchased = false; // 是否已购买

    @Column(name = "category", length = 50)
    private String category; // 分类

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
