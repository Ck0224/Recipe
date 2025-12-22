package com.recipe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ingredients")
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id") // 关键：移除 nullable = false
    private Recipe recipe;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity;

    @JsonProperty("quantity")
    public void setQuantityFromNumber(Number number) {
        if (number != null) {
            this.quantity = new BigDecimal(number.toString());
        }
    }

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "note", length = 200)
    private String note;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setQuantity(Integer quantity) {
        if (quantity != null) {
            this.quantity = new BigDecimal(quantity);
        }
    }

    public void setQuantity(Double quantity) {
        if (quantity != null) {
            this.quantity = new BigDecimal(quantity);
        }
    }
}
