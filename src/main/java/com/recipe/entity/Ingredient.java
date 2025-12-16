package com.recipe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore; // 已导入，直接用
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

    // 关键修复：添加@JsonIgnore，排除反向关联的recipe序列化
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 食材名称

    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity; // 数量

    @Column(name = "unit", length = 20)
    private String unit; // 单位（克/勺/杯等）

    @Column(name = "note", length = 200)
    private String note; // 备注（如"去皮"、"切丁"）

    @Column(name = "sort_order")
    private Integer sortOrder = 0; // 排序序号
}
