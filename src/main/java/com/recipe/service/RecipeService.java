package com.recipe.service;

import com.recipe.entity.Recipe;
import com.recipe.entity.UserInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 食谱核心业务接口（完整适配：权限控制 + ID+分类+菜名+食材+难度 多条件搜索）
 */
public interface RecipeService {
    // 1. 创建食谱
    Recipe createRecipe(Recipe recipe, Long userId);

    // 2. 根据ID查询食谱（含食材+步骤）+ 权限校验
    Recipe getRecipeById(Long id, Long currentUserId); // 新增：当前登录用户ID（用于权限判断）

    // ========== 核心新增：权限相关查询 ==========
    // 3. 查询用户可见的食谱（公开 + 自己的私有）
    Page<Recipe> getUserVisibleRecipes(
            Long currentUserId,  // 当前登录用户ID
            Long id,             // 食谱ID
            String title,        // 菜名
            String category,     // 分类
            String difficulty,   // 难度
            Pageable pageable);

    // 4. 带权限的多条件查询（管理员看所有，普通用户看可见）
    Page<Recipe> getRecipesByMultiConditionsWithPermission(
            Long currentUserId,  // 当前登录用户ID
            Boolean isAdmin,     // 是否管理员
            Long id,             // 食谱ID
            String title,        // 菜名
            String category,     // 分类
            String ingredient,   // 食材
            String difficulty,   // 难度
            Pageable pageable);

    // 5. 按用户ID查询个人食谱（我的食谱）
    Page<Recipe> getRecipesByUserId(Long userId, Pageable pageable);

    // ========== 兼容原有多条件查询（默认仅查公开） ==========
    Page<Recipe> getRecipesByMultiConditions(
            Long id,          // 食谱ID（正整数）
            String title,     // 菜名
            String category,  // 分类
            String ingredient,// 食材
            String difficulty,// 难度（String类型，适配前端）
            Pageable pageable);

    // 6. 更新食谱（仅作者/管理员可更）
    Recipe updateRecipe(Long id, Recipe recipe, Long currentUserId, Boolean isAdmin); // 新增：管理员标识

    // 7. 删除食谱（仅作者/管理员可删）
    void deleteRecipe(Long id, Long currentUserId, Boolean isAdmin); // 新增：管理员标识

    // 8. 根据用户库存食材推荐食谱（含自己的私有）
    List<Recipe> recommendRecipesByInventory(Long userId);

    // 9. 食谱点赞
    Recipe likeRecipe(Long id);

    // 10. 食谱浏览量+1
    Recipe incrementView(Long id);
}
