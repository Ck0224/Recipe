package com.recipe.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(name = "prep_time")
    private Integer prepTime; // 准备时间（分钟）

    @Column(name = "cook_time", nullable = false)
    private Integer cookTime; // 烹饪时间（分钟）

    @Column(name = "servings")
    private Integer servings; // 份数

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Column(name = "category", length = 50)
    private String category; // 菜系（中餐/西餐/甜点等）

    @Transient // 不映射到数据库
    private List<String> tagList;

    @Column(name = "tags", columnDefinition = "JSON")
    private String tags; // 存储JSON格式字符串

    // 设置tagList时自动转为tags字符串
    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
        try {
            this.tags = new ObjectMapper().writeValueAsString(tagList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("标签转换失败", e);
        }
    }

    @Column(name = "views")
    private Integer views = 0;

    @Column(name = "likes")
    private Integer likes = 0;

    @Column(name = "is_private")
    private Boolean isPrivate = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 关联食材（级联删除/更新）
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<Ingredient> ingredients;

    // 关联步骤（级联删除/更新）
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<Step> steps;

    // 关联收藏
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites;

    // 关联膳食计划
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealPlan> mealPlans;

    // 难度枚举
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}
