package com.recipe.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 食谱返回DTO（屏蔽冗余字段，适配前端展示）
 */
@Data
public class RecipeDTO {
    // 食谱核心字段
    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private Integer prepTime;
    private Integer cookTime;
    private Integer servings;
    private String difficulty; // 枚举转字符串（EASY/MEDIUM/HARD）
    private String category;
    private List<String> tagList;
    private Boolean isPrivate;
    private Integer views;
    private Integer likes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联用户（仅返回核心信息）
    private UserSimpleDTO user;

    // 关联食材
    private List<IngredientDTO> ingredients;

    // 关联步骤
    private List<StepDTO> steps;

    // 嵌套DTO：简化用户信息（无密码/关联集合）
    @Data
    public static class UserSimpleDTO {
        private Long id;
        private String username;
        private String email;
        private String avatarUrl;
        // 核心新增：管理员标识字段（Lombok自动生成setIsAdmin/getIsAdmin）
        private Boolean isAdmin;
    }

    // 嵌套DTO：食材信息
    @Data
    public static class IngredientDTO {
        private Long id;
        private String name;
        private BigDecimal quantity;
        private String unit;
        private String note;
        private Integer sortOrder;
    }

    // 嵌套DTO：步骤信息
    @Data
    public static class StepDTO {
        private Long id;
        private Integer stepNumber;
        private String description;
        private String imageUrl;
        private Integer timerMinutes;
        private Integer sortOrder;
    }
}
