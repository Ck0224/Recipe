package com.recipe.service.impl;

import com.recipe.entity.Ingredient;
import com.recipe.entity.Recipe;
import com.recipe.entity.Step;
import com.recipe.entity.User;
import com.recipe.repository.IngredientRepository;
import com.recipe.repository.RecipeRepository;
import com.recipe.repository.StepRepository;
import com.recipe.repository.UserRepository;
import com.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final StepRepository stepRepository;

    // 注入JdbcTemplate用于原生SQL操作
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public Recipe createRecipe(Recipe recipe, Long userId) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(recipe, "食谱信息不能为空");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("创建失败：用户不存在，ID：" + userId));
        recipe.setUser(user);

        if (recipe.getIsPrivate() == null) {
            recipe.setIsPrivate(false);
        }
        if (recipe.getViews() == null) {
            recipe.setViews(0);
        }
        if (recipe.getLikes() == null) {
            recipe.setLikes(0);
        }

        // 1. 先保存食谱获取ID
        Recipe savedRecipe = recipeRepository.save(recipe);
        Long recipeId = savedRecipe.getId();
        System.out.println("食谱保存成功，ID：" + recipeId);

        // 2. 原生SQL插入食材（彻底绕开JPA级联）
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            System.out.println("准备插入食材数量：" + recipe.getIngredients().size());
            for (Ingredient ingredient : recipe.getIngredients()) {
                String sql = "INSERT INTO ingredients (name, note, quantity, recipe_id, sort_order, unit) VALUES (?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(
                        sql,
                        ingredient.getName().trim(),
                        ingredient.getNote() == null ? "" : ingredient.getNote().trim(),
                        ingredient.getQuantity() == null ? 0 : ingredient.getQuantity(),
                        recipeId, // 直接传ID，绝对不为空
                        ingredient.getSortOrder() == null ? 0 : ingredient.getSortOrder(),
                        ingredient.getUnit() == null ? "" : ingredient.getUnit().trim()
                );
                System.out.println("食材插入成功：" + ingredient.getName() + " | recipe_id: " + recipeId);
            }
        }

        // 3. 原生SQL插入步骤
        if (recipe.getSteps() != null && !recipe.getSteps().isEmpty()) {
            System.out.println("准备插入步骤数量：" + recipe.getSteps().size());
            for (Step step : recipe.getSteps()) {
                String sql = "INSERT INTO steps (step_number, description, image_url, timer_minutes, sort_order, recipe_id) VALUES (?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(
                        sql,
                        step.getStepNumber() == null ? 0 : step.getStepNumber(),
                        step.getDescription().trim(),
                        step.getImageUrl() == null ? "" : step.getImageUrl().trim(),
                        step.getTimerMinutes() == null ? 0 : step.getTimerMinutes(),
                        step.getSortOrder() == null ? 0 : step.getSortOrder(),
                        recipeId // 直接传ID
                );
                System.out.println("步骤插入成功：步骤" + step.getStepNumber() + " | recipe_id: " + recipeId);
            }
        }

        return savedRecipe;
    }

    @Override
    public Recipe getRecipeById(Long id, Long currentUserId) {
        Assert.notNull(id, "食谱ID不能为空");
        Assert.notNull(currentUserId, "当前用户ID不能为空");

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食谱不存在，ID：" + id));

        boolean isOwner = recipe.getUser().getId().equals(currentUserId);
        Boolean isAdmin = userRepository.isAdmin(currentUserId);

        if (!recipe.getIsPrivate() || isOwner || Boolean.TRUE.equals(isAdmin)) {
            List<Ingredient> ingredients = ingredientRepository.findByRecipeIdOrderBySortOrderAsc(id);
            List<Step> steps = stepRepository.findByRecipeIdOrderByStepNumberAsc(id);
            recipe.setIngredients(ingredients);
            recipe.setSteps(steps);

            System.out.println("食谱ID: " + id + " → 食材数量: " + ingredients.size() + "，步骤数量: " + steps.size());
            return recipe;
        }

        throw new RuntimeException("无权限查看该私有食谱，ID：" + id);
    }

    // 以下方法保持不变
    @Override
    public Page<Recipe> getUserVisibleRecipes(
            Long currentUserId, Long id, String title, String category, String difficulty, Pageable pageable) {
        Assert.notNull(currentUserId, "当前用户ID不能为空");
        Long finalId = (id != null && id > 0) ? id : null;
        String finalTitle = (title == null || title.trim().isEmpty()) ? null : title.trim();
        String finalCategory = (category == null || category.trim().isEmpty()) ? null : category.trim();
        String finalDifficulty = (difficulty == null || difficulty.trim().isEmpty()) ? null : difficulty.trim();
        return recipeRepository.findUserVisibleRecipes(
                currentUserId, finalId, finalTitle, finalCategory, finalDifficulty, pageable);
    }

    @Override
    public Page<Recipe> getRecipesByMultiConditionsWithPermission(
            Long currentUserId, Boolean isAdmin, Long id, String title,
            String category, String ingredient, String difficulty, Pageable pageable) {
        Assert.notNull(currentUserId, "当前用户ID不能为空");
        Assert.notNull(isAdmin, "管理员标识不能为空");
        Long finalId = (id != null && id > 0) ? id : null;
        String finalTitle = (title == null || title.trim().isEmpty()) ? null : title.trim();
        String finalCategory = (category == null || category.trim().isEmpty()) ? null : category.trim();
        String finalIngredient = (ingredient == null || ingredient.trim().isEmpty()) ? null : ingredient.trim();
        String finalDifficulty = (difficulty == null || difficulty.trim().isEmpty()) ? null : difficulty.trim();

        // 修正1：方法名从 findRecipesByMultiConditionsWithPermission → findByMultiConditionsWithPermission
        // 修正2：参数名对齐（Repository中是 userId，不是 currentUserId）
        return recipeRepository.findByMultiConditionsWithPermission(
                currentUserId, isAdmin, finalId, finalTitle,
                finalCategory, finalIngredient, finalDifficulty, pageable);
    }

    @Override
    public Page<Recipe> getRecipesByUserId(Long userId, Pageable pageable) {
        Assert.notNull(userId, "用户ID不能为空");
        return recipeRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Recipe> getRecipesByMultiConditions(
            Long id, String title, String category, String ingredient, String difficulty, Pageable pageable) {
        Long finalId = (id != null && id > 0) ? id : null;
        String finalTitle = (title == null || title.trim().isEmpty()) ? null : title.trim();
        String finalCategory = (category == null || category.trim().isEmpty()) ? null : category.trim();
        String finalIngredient = (ingredient == null || ingredient.trim().isEmpty()) ? null : ingredient.trim();
        String finalDifficulty = (difficulty == null || difficulty.trim().isEmpty()) ? null : difficulty.trim();

        // 修正：方法名从 findRecipesByMultiConditions → findByMultiConditions
        return recipeRepository.findByMultiConditions(
                finalId, finalTitle, finalCategory, finalIngredient, finalDifficulty, pageable);
    }

    // ========== 核心修改：仅在这个方法中新增食材/步骤更新逻辑 ==========
    @Override
    @Transactional // 新增事务注解，保证食材/步骤和食谱一起更新
    public Recipe updateRecipe(Long id, Recipe recipe, Long currentUserId, Boolean isAdmin) {
        Assert.notNull(id, "食谱ID不能为空");
        Assert.notNull(currentUserId, "当前用户ID不能为空");
        Assert.notNull(isAdmin, "管理员标识不能为空");
        Assert.notNull(recipe, "食谱信息不能为空");

        Recipe existingRecipe = getRecipeById(id, currentUserId);
        boolean isOwner = existingRecipe.getUser().getId().equals(currentUserId);
        if (!isOwner && !Boolean.TRUE.equals(isAdmin)) {
            throw new RuntimeException("无权限修改该食谱（仅作者或管理员可操作），ID：" + id);
        }

        recipe.setId(id);
        recipe.setUser(existingRecipe.getUser());
        // 1. 保存食谱基础信息（原有逻辑）
        Recipe updatedRecipe = recipeRepository.save(recipe);

        // ========== 新增：食材/步骤更新逻辑（和createRecipe保持一致的原生SQL） ==========
        Long recipeId = id;
        // 2. 删除原有食材（原生SQL）
        String deleteIngredientSql = "DELETE FROM ingredients WHERE recipe_id = ?";
        jdbcTemplate.update(deleteIngredientSql, recipeId);
        System.out.println("删除食谱ID: " + recipeId + " 的所有原有食材");

        // 3. 插入新食材
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            System.out.println("准备更新食材数量：" + recipe.getIngredients().size());
            for (Ingredient ingredient : recipe.getIngredients()) {
                String insertIngredientSql = "INSERT INTO ingredients (name, note, quantity, recipe_id, sort_order, unit) VALUES (?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(
                        insertIngredientSql,
                        ingredient.getName().trim(),
                        ingredient.getNote() == null ? "" : ingredient.getNote().trim(),
                        ingredient.getQuantity() == null ? 0 : ingredient.getQuantity(),
                        recipeId,
                        ingredient.getSortOrder() == null ? 0 : ingredient.getSortOrder(),
                        ingredient.getUnit() == null ? "" : ingredient.getUnit().trim()
                );
                System.out.println("更新食材成功：" + ingredient.getName() + " | recipe_id: " + recipeId);
            }
        }

        // 4. 删除原有步骤（原生SQL）
        String deleteStepSql = "DELETE FROM steps WHERE recipe_id = ?";
        jdbcTemplate.update(deleteStepSql, recipeId);
        System.out.println("删除食谱ID: " + recipeId + " 的所有原有步骤");

        // 5. 插入新步骤
        if (recipe.getSteps() != null && !recipe.getSteps().isEmpty()) {
            System.out.println("准备更新步骤数量：" + recipe.getSteps().size());
            for (Step step : recipe.getSteps()) {
                String insertStepSql = "INSERT INTO steps (step_number, description, image_url, timer_minutes, sort_order, recipe_id) VALUES (?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(
                        insertStepSql,
                        step.getStepNumber() == null ? 0 : step.getStepNumber(),
                        step.getDescription().trim(),
                        step.getImageUrl() == null ? "" : step.getImageUrl().trim(),
                        step.getTimerMinutes() == null ? 0 : step.getTimerMinutes(),
                        step.getSortOrder() == null ? 0 : step.getSortOrder(),
                        recipeId
                );
                System.out.println("更新步骤成功：步骤" + step.getStepNumber() + " | recipe_id: " + recipeId);
            }
        }
        // ========== 新增逻辑结束 ==========

        // 6. 返回包含最新食材/步骤的食谱（原有逻辑增强）
        return getRecipeById(id, currentUserId);
    }

    @Override
    public void deleteRecipe(Long id, Long currentUserId, Boolean isAdmin) {
        Assert.notNull(id, "食谱ID不能为空");
        Assert.notNull(currentUserId, "当前用户ID不能为空");
        Assert.notNull(isAdmin, "管理员标识不能为空");

        Recipe recipe = getRecipeById(id, currentUserId);
        boolean isOwner = recipe.getUser().getId().equals(currentUserId);
        if (!isOwner && !Boolean.TRUE.equals(isAdmin)) {
            throw new RuntimeException("无权限删除该食谱（仅作者或管理员可操作），ID：" + id);
        }

        recipeRepository.deleteById(id);
    }

    @Override
    public List<Recipe> recommendRecipesByInventory(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");
        List<String> ingredientNames = List.of();
        return recipeRepository.findRecipesByIngredients(userId, ingredientNames);
    }

    @Override
    public Recipe likeRecipe(Long id) {
        Assert.notNull(id, "食谱ID不能为空");
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食谱不存在，ID：" + id));
        recipe.setLikes(recipe.getLikes() + 1);
        return recipeRepository.save(recipe);
    }

    @Override
    public Recipe incrementView(Long id) {
        Assert.notNull(id, "食谱ID不能为空");
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食谱不存在，ID：" + id));
        recipe.setViews(recipe.getViews() + 1);
        return recipeRepository.save(recipe);
    }

    @Deprecated
    public Recipe getRecipeById(Long id) {
        return getRecipeById(id, -1L);
    }

    @Deprecated
    public Recipe updateRecipe(Long id, Recipe recipe, Long userId) {
        Boolean isAdmin = userRepository.isAdmin(userId);
        return updateRecipe(id, recipe, userId, isAdmin);
    }

    @Deprecated
    public void deleteRecipe(Long id, Long userId) {
        Boolean isAdmin = userRepository.isAdmin(userId);
        deleteRecipe(id, userId, isAdmin);
    }
}
