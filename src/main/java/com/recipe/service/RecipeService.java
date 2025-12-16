package com.recipe.service;

import com.recipe.entity.Recipe;
import com.recipe.entity.UserInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 食谱核心业务接口（完整适配：ID+分类+菜名+食材+难度 多条件搜索）
 */
public interface RecipeService {
    // 1. 创建食谱
    Recipe createRecipe(Recipe recipe, Long userId);

    // 2. 根据ID查询食谱（含食材+步骤）
    Recipe getRecipeById(Long id);

    // ========== 废弃冗余方法（功能已合并到多条件查询） ==========
    // 原3/4/5/7方法可删除，功能已完全覆盖，若需兼容旧代码可保留并内部调用多条件查询
    // 3. 分页查询公开食谱 → 多条件查询不传参数即可实现
    // Page<Recipe> getPublicRecipes(Pageable pageable);
    // 4. 按分类查询公开食谱 → 多条件查询仅传category参数即可实现
    // Page<Recipe> getRecipesByCategory(String category, Pageable pageable);
    // 5. 按难度查询公开食谱 → 多条件查询仅传difficulty参数即可实现
    // Page<Recipe> getRecipesByDifficulty(Recipe.Difficulty difficulty, Pageable pageable);
    // 7. 关键词搜索食谱（标题/描述） → 多条件查询仅传title参数即可实现
    // Page<Recipe> searchPublicRecipesByKeyword(String keyword, Pageable pageable);

    // 6. 按用户ID查询个人食谱（保留：个人中心核心功能）
    Page<Recipe> getRecipesByUserId(Long userId, Pageable pageable);

    // ========== 核心修改：多条件组合查询（新增title参数） ==========
    Page<Recipe> getRecipesByMultiConditions(
            Long id,          // 食谱ID（正整数）
            String title,     // 菜名（核心新增）
            String category,  // 分类
            String ingredient,// 食材
            String difficulty,// 难度（String类型，适配前端）
            Pageable pageable);

    // 8. 更新食谱（仅作者可更）
    Recipe updateRecipe(Long id, Recipe recipe, Long userId);

    // 9. 删除食谱（仅作者可删）
    void deleteRecipe(Long id, Long userId);

    // 10. 根据用户库存食材推荐食谱
    List<Recipe> recommendRecipesByInventory(Long userId);

    // 11. 食谱点赞
    Recipe likeRecipe(Long id);

    // 12. 食谱浏览量+1
    Recipe incrementView(Long id);
}
