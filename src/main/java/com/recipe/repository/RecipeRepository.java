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

    // ========== 基础查询（保留核心必要方法） ==========
    // 按用户ID分页查询私有/公开食谱
    Page<Recipe> findByUserId(Long userId, Pageable pageable);

    // 查询所有公开食谱（无筛选）
    Page<Recipe> findByIsPrivateFalse(Pageable pageable);

    // ========== 多条件组合查询（核心：支持ID+分类+菜名+食材+难度） ==========
    @Query(value = "SELECT r.* FROM recipes r " +
            "LEFT JOIN ingredients i ON r.id = i.recipe_id " +
            "WHERE (r.id = :id OR :id IS NULL) " +
            "AND (r.title LIKE %:title% OR :title IS NULL) " +  // 菜名模糊搜索
            "AND (r.category LIKE %:category% OR :category IS NULL) " +  // 分类模糊搜索
            "AND (i.name LIKE %:ingredient% OR :ingredient IS NULL) " +  // 食材模糊搜索
            "AND (r.difficulty = :difficulty OR :difficulty IS NULL) " +  // 难度精准匹配
            "AND r.is_private = false " +
            "GROUP BY r.id",  // 避免食材关联导致重复数据
            countQuery = "SELECT COUNT(DISTINCT r.id) FROM recipes r " +
                    "LEFT JOIN ingredients i ON r.id = i.recipe_id " +
                    "WHERE (r.id = :id OR :id IS NULL) " +
                    "AND (r.title LIKE %:title% OR :title IS NULL) " +
                    "AND (r.category LIKE %:category% OR :category IS NULL) " +
                    "AND (i.name LIKE %:ingredient% OR :ingredient IS NULL) " +
                    "AND (r.difficulty = :difficulty OR :difficulty IS NULL) " +
                    "AND r.is_private = false",
            nativeQuery = true)
    Page<Recipe> findByMultiConditions(
            @Param("id") Long id,
            @Param("title") String title,       // 菜名（新增核心参数）
            @Param("category") String category, // 分类
            @Param("ingredient") String ingredient, // 食材
            @Param("difficulty") String difficulty, // 难度（String适配前端）
            Pageable pageable);

    // ========== 食材推荐查询（保留业务必要方法） ==========
    @Query(value = "SELECT r.* FROM recipes r " +
            "JOIN ingredients i ON r.id = i.recipe_id " +
            "WHERE r.is_private = false AND i.name IN :ingredientNames " +
            "GROUP BY r.id ORDER BY COUNT(i.id) DESC",
            nativeQuery = true)
    List<Recipe> findRecipesByIngredients(@Param("ingredientNames") List<String> ingredientNames);
}
