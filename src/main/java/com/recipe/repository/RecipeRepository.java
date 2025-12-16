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

    // ========== 基础查询（保留+增强） ==========
    // 按用户ID分页查询私有/公开食谱（我的食谱）
    Page<Recipe> findByUserId(Long userId, Pageable pageable);

    // 查询所有公开食谱（无筛选）
    Page<Recipe> findByIsPrivateFalse(Pageable pageable);

    // ========== 核心新增：用户可见食谱查询（公开+自己的私有） ==========
    @Query(value = "SELECT r.* FROM recipes r " +
            "WHERE (r.is_private = false OR r.user_id = :userId) " +
            "AND (r.id = :id OR :id IS NULL) " +
            "AND (r.title LIKE %:title% OR :title IS NULL) " +
            "AND (r.category LIKE %:category% OR :category IS NULL) " +
            "AND (r.difficulty = :difficulty OR :difficulty IS NULL) " +
            "GROUP BY r.id",
            countQuery = "SELECT COUNT(DISTINCT r.id) FROM recipes r " +
                    "WHERE (r.is_private = false OR r.user_id = :userId) " +
                    "AND (r.id = :id OR :id IS NULL) " +
                    "AND (r.title LIKE %:title% OR :title IS NULL) " +
                    "AND (r.category LIKE %:category% OR :category IS NULL) " +
                    "AND (r.difficulty = :difficulty OR :difficulty IS NULL)",
            nativeQuery = true)
    Page<Recipe> findUserVisibleRecipes(
            @Param("userId") Long userId,          // 当前登录用户ID
            @Param("id") Long id,                  // 食谱ID
            @Param("title") String title,          // 菜名
            @Param("category") String category,    // 分类
            @Param("difficulty") String difficulty,// 难度
            Pageable pageable);

    // ========== 增强：多条件组合查询（支持管理员查看所有+普通用户仅看可见） ==========
    @Query(value = "SELECT r.* FROM recipes r " +
            "LEFT JOIN ingredients i ON r.id = i.recipe_id " +
            "WHERE (r.id = :id OR :id IS NULL) " +
            "AND (r.title LIKE %:title% OR :title IS NULL) " +
            "AND (r.category LIKE %:category% OR :category IS NULL) " +
            "AND (i.name LIKE %:ingredient% OR :ingredient IS NULL) " +
            "AND (r.difficulty = :difficulty OR :difficulty IS NULL) " +
            // 核心：管理员看所有，普通用户看公开+自己的私有
            "AND (:isAdmin = true OR r.is_private = false OR r.user_id = :userId) " +
            "GROUP BY r.id",
            countQuery = "SELECT COUNT(DISTINCT r.id) FROM recipes r " +
                    "LEFT JOIN ingredients i ON r.id = i.recipe_id " +
                    "WHERE (r.id = :id OR :id IS NULL) " +
                    "AND (r.title LIKE %:title% OR :title IS NULL) " +
                    "AND (r.category LIKE %:category% OR :category IS NULL) " +
                    "AND (i.name LIKE %:ingredient% OR :ingredient IS NULL) " +
                    "AND (r.difficulty = :difficulty OR :difficulty IS NULL) " +
                    "AND (:isAdmin = true OR r.is_private = false OR r.user_id = :userId)",
            nativeQuery = true)
    Page<Recipe> findByMultiConditionsWithPermission(
            @Param("userId") Long userId,          // 当前登录用户ID
            @Param("isAdmin") Boolean isAdmin,     // 是否管理员
            @Param("id") Long id,                  // 食谱ID
            @Param("title") String title,          // 菜名
            @Param("category") String category,    // 分类
            @Param("ingredient") String ingredient,// 食材
            @Param("difficulty") String difficulty,// 难度
            Pageable pageable);

    // ========== 兼容原有多条件查询（默认仅查公开） ==========
    @Query(value = "SELECT r.* FROM recipes r " +
            "LEFT JOIN ingredients i ON r.id = i.recipe_id " +
            "WHERE (r.id = :id OR :id IS NULL) " +
            "AND (r.title LIKE %:title% OR :title IS NULL) " +
            "AND (r.category LIKE %:category% OR :category IS NULL) " +
            "AND (i.name LIKE %:ingredient% OR :ingredient IS NULL) " +
            "AND (r.difficulty = :difficulty OR :difficulty IS NULL) " +
            "AND r.is_private = false " +
            "GROUP BY r.id",
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
            @Param("title") String title,
            @Param("category") String category,
            @Param("ingredient") String ingredient,
            @Param("difficulty") String difficulty,
            Pageable pageable);

    // ========== 食材推荐查询（保留+权限优化） ==========
    @Query(value = "SELECT r.* FROM recipes r " +
            "JOIN ingredients i ON r.id = i.recipe_id " +
            "WHERE (r.is_private = false OR r.user_id = :userId) " + // 新增：可见性判断
            "AND i.name IN :ingredientNames " +
            "GROUP BY r.id ORDER BY COUNT(i.id) DESC",
            nativeQuery = true)
    List<Recipe> findRecipesByIngredients(
            @Param("userId") Long userId,                  // 新增：当前用户ID
            @Param("ingredientNames") List<String> ingredientNames);
}
