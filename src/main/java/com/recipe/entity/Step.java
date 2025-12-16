package com.recipe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore; // 新增导入

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "steps")
public class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关键修复：添加@JsonIgnore，排除反向关联的recipe序列化
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber; // 步骤编号

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description; // 步骤描述

    @Column(name = "image_url", length = 500)
    private String imageUrl; // 步骤图片

    @Column(name = "timer_minutes")
    private Integer timerMinutes; // 计时（分钟）

    @Column(name = "sort_order")
    private Integer sortOrder = 0; // 排序序号
}
