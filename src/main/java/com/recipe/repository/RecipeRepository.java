package com.recipe.repository;

import com.recipe.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    // 按用户ID分页查询
    Page<Recipe> findByUserId(Long userId, Pageable pageable);

    // 公开食谱
    Page<Recipe> findByIsPrivateFalse(Pageable pageable);

    // 按分类查公开食谱
    Page<Recipe> findByCategoryAndIsPrivateFalse(String category, Pageable pageable);

    // 按难度查公开食谱
    Page<Recipe> findByDifficultyAndIsPrivateFalse(Recipe.Difficulty difficulty, Pageable pageable);

    // 关键词搜索（标题/描述）
    @Query("SELECT r FROM Recipe r WHERE r.isPrivate = false AND (r.title LIKE %:keyword% OR r.description LIKE %:keyword%)")
    Page<Recipe> searchPublicRecipes(@Param("keyword") String keyword, Pageable pageable);

    // 根据食材匹配推荐
    @Query(value = "SELECT r.* FROM recipes r " +
            "JOIN ingredients i ON r.id = i.recipe_id " +
            "WHERE r.is_private = false AND i.name IN :ingredientNames " +
            "GROUP BY r.id ORDER BY COUNT(i.id) DESC",
            nativeQuery = true)
    List<Recipe> findRecipesByIngredients(@Param("ingredientNames") List<String> ingredientNames);
}
