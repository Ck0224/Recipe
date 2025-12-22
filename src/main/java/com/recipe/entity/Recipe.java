package com.recipe.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
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
    private Integer prepTime;

    @Column(name = "cook_time", nullable = false)
    private Integer cookTime;

    @Column(name = "servings")
    private Integer servings;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Column(name = "category", length = 50)
    private String category;

    @Transient
    private List<String> tagList;

    @Column(name = "tags", columnDefinition = "JSON")
    private String tags;

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

    @Column(name = "is_private", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isPrivate = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 关键修改：添加 @Transient 禁止JPA级联
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @Transient  //标记为临时字段，不参与JPA持久化
    private List<Ingredient> ingredients;

    // 关键修改：添加 @Transient 禁止JPA级联
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stepNumber ASC")
     @Transient //标记为临时字段，不参与JPA持久化
    private List<Step> steps;

    @JsonIgnore
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Favorite> favorites;

    @JsonIgnore
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MealPlan> mealPlans;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public List<String> getTagList() {
        if (this.tagList == null && this.tags != null) {
            try {
                this.tagList = new ObjectMapper().readValue(this.tags, List.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("标签反序列化失败", e);
            }
        }
        return this.tagList;
    }
}
