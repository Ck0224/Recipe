package com.recipe.service;

import com.recipe.entity.Recipe;
import com.recipe.entity.UserInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 食谱核心业务接口
 */
public interface RecipeService {
    // 1. 创建食谱
    Recipe createRecipe(Recipe recipe, Long userId);

    // 2. 根据ID查询食谱（含食材+步骤）
    Recipe getRecipeById(Long id);

    // 3. 分页查询公开食谱
    Page<Recipe> getPublicRecipes(Pageable pageable);

    // 4. 按分类查询公开食谱
    Page<Recipe> getRecipesByCategory(String category, Pageable pageable);

    // 5. 按难度查询公开食谱
    Page<Recipe> getRecipesByDifficulty(Recipe.Difficulty difficulty, Pageable pageable);

    // 6. 按用户ID查询个人食谱
    Page<Recipe> getRecipesByUserId(Long userId, Pageable pageable);

    // 7. 关键词搜索食谱（标题/描述）
    Page<Recipe> searchRecipes(String keyword, Pageable pageable);

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
