package com.recipe.service.impl;

import com.recipe.entity.Recipe;
import com.recipe.entity.User;
import com.recipe.repository.RecipeRepository;
import com.recipe.repository.UserRepository;
import com.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository; // 新增：注入用户仓库用于权限判断

    // 1. 创建食谱（增强：关联用户+初始化字段）
    @Override
    public Recipe createRecipe(Recipe recipe, Long userId) {
        // 参数校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(recipe, "食谱信息不能为空");

        // 关联创建用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("创建失败：用户不存在，ID：" + userId));
        recipe.setUser(user);

        // 初始化默认值（确保私有字段非空）
        if (recipe.getIsPrivate() == null) {
            recipe.setIsPrivate(false); // 默认公开
        }
        if (recipe.getViews() == null) {
            recipe.setViews(0);
        }
        if (recipe.getLikes() == null) {
            recipe.setLikes(0);
        }

        return recipeRepository.save(recipe);
    }

    // 2. 根据ID查询食谱（新增：权限校验）
    @Override
    public Recipe getRecipeById(Long id, Long currentUserId) {
        // 参数校验
        Assert.notNull(id, "食谱ID不能为空");
        Assert.notNull(currentUserId, "当前用户ID不能为空");

        // 查询食谱
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食谱不存在，ID：" + id));

        // 权限判断：公开食谱 | 自己创建的 | 管理员
        boolean isOwner = recipe.getUser().getId().equals(currentUserId);
        Boolean isAdmin = userRepository.isAdmin(currentUserId);

        if (!recipe.getIsPrivate() || isOwner || Boolean.TRUE.equals(isAdmin)) {
            return recipe;
        }

        throw new RuntimeException("无权限查看该私有食谱，ID：" + id);
    }

    // 3. 查询用户可见的食谱（公开 + 自己的私有）
    @Override
    public Page<Recipe> getUserVisibleRecipes(
            Long currentUserId, Long id, String title, String category, String difficulty, Pageable pageable) {
        // 参数校验
        Assert.notNull(currentUserId, "当前用户ID不能为空");

        // 空值处理
        Long finalId = (id != null && id > 0) ? id : null;
        String finalTitle = (title == null || title.trim().isEmpty()) ? null : title.trim();
        String finalCategory = (category == null || category.trim().isEmpty()) ? null : category.trim();
        String finalDifficulty = (difficulty == null || difficulty.trim().isEmpty()) ? null : difficulty.trim();

        // 调用仓库层权限查询
        return recipeRepository.findUserVisibleRecipes(
                currentUserId, finalId, finalTitle, finalCategory, finalDifficulty, pageable);
    }

    // 4. 带权限的多条件查询（管理员看所有，普通用户看可见）
    @Override
    public Page<Recipe> getRecipesByMultiConditionsWithPermission(
            Long currentUserId, Boolean isAdmin, Long id, String title,
            String category, String ingredient, String difficulty, Pageable pageable) {
        // 参数校验
        Assert.notNull(currentUserId, "当前用户ID不能为空");
        Assert.notNull(isAdmin, "管理员标识不能为空");

        // 空值处理
        Long finalId = (id != null && id > 0) ? id : null;
        String finalTitle = (title == null || title.trim().isEmpty()) ? null : title.trim();
        String finalCategory = (category == null || category.trim().isEmpty()) ? null : category.trim();
        String finalIngredient = (ingredient == null || ingredient.trim().isEmpty()) ? null : ingredient.trim();
        String finalDifficulty = (difficulty == null || difficulty.trim().isEmpty()) ? null : difficulty.trim();

        // 调用仓库层带权限的多条件查询
        return recipeRepository.findByMultiConditionsWithPermission(
                currentUserId, isAdmin, finalId, finalTitle,
                finalCategory, finalIngredient, finalDifficulty, pageable);
    }

    // 5. 按用户ID查询个人食谱（保留+增强校验）
    @Override
    public Page<Recipe> getRecipesByUserId(Long userId, Pageable pageable) {
        Assert.notNull(userId, "用户ID不能为空");
        return recipeRepository.findByUserId(userId, pageable);
    }

    // 兼容原有多条件查询（默认仅查公开）
    @Override
    public Page<Recipe> getRecipesByMultiConditions(
            Long id, String title, String category, String ingredient, String difficulty, Pageable pageable) {
        // 空值处理
        Long finalId = (id != null && id > 0) ? id : null;
        String finalTitle = (title == null || title.trim().isEmpty()) ? null : title.trim();
        String finalCategory = (category == null || category.trim().isEmpty()) ? null : category.trim();
        String finalIngredient = (ingredient == null || ingredient.trim().isEmpty()) ? null : ingredient.trim();
        String finalDifficulty = (difficulty == null || difficulty.trim().isEmpty()) ? null : difficulty.trim();

        return recipeRepository.findByMultiConditions(
                finalId, finalTitle, finalCategory, finalIngredient, finalDifficulty, pageable);
    }

    // 6. 更新食谱（新增：权限校验）
    @Override
    public Recipe updateRecipe(Long id, Recipe recipe, Long currentUserId, Boolean isAdmin) {
        // 参数校验
        Assert.notNull(id, "食谱ID不能为空");
        Assert.notNull(currentUserId, "当前用户ID不能为空");
        Assert.notNull(isAdmin, "管理员标识不能为空");
        Assert.notNull(recipe, "食谱信息不能为空");

        // 先查询并校验权限
        Recipe existingRecipe = getRecipeById(id, currentUserId);
        boolean isOwner = existingRecipe.getUser().getId().equals(currentUserId);

        // 仅作者或管理员可修改
        if (!isOwner && !Boolean.TRUE.equals(isAdmin)) {
            throw new RuntimeException("无权限修改该食谱（仅作者或管理员可操作），ID：" + id);
        }

        // 保留原有ID和创建用户（防止篡改）
        recipe.setId(id);
        recipe.setUser(existingRecipe.getUser());

        return recipeRepository.save(recipe);
    }

    // 7. 删除食谱（新增：权限校验）
    @Override
    public void deleteRecipe(Long id, Long currentUserId, Boolean isAdmin) {
        // 参数校验
        Assert.notNull(id, "食谱ID不能为空");
        Assert.notNull(currentUserId, "当前用户ID不能为空");
        Assert.notNull(isAdmin, "管理员标识不能为空");

        // 查询并校验权限
        Recipe recipe = getRecipeById(id, currentUserId);
        boolean isOwner = recipe.getUser().getId().equals(currentUserId);

        // 仅作者或管理员可删除
        if (!isOwner && !Boolean.TRUE.equals(isAdmin)) {
            throw new RuntimeException("无权限删除该食谱（仅作者或管理员可操作），ID：" + id);
        }

        recipeRepository.deleteById(id);
    }

    // 8. 食材推荐（增强：含自己的私有食谱）
    @Override
    public List<Recipe> recommendRecipesByInventory(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");
        // 实际业务逻辑需替换：查询用户库存食材 → 调用仓库层带权限的推荐查询
        // 示例：List<String> ingredientNames = userInventoryRepository.findIngredientNamesByUserId(userId);
        List<String> ingredientNames = List.of(); // 替换为实际库存食材列表
        return recipeRepository.findRecipesByIngredients(userId, ingredientNames);
    }

    // 9. 食谱点赞（保留+增强）
    @Override
    public Recipe likeRecipe(Long id) {
        Assert.notNull(id, "食谱ID不能为空");
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食谱不存在，ID：" + id));

        recipe.setLikes(recipe.getLikes() + 1);
        return recipeRepository.save(recipe);
    }

    // 10. 浏览量+1（保留+增强）
    @Override
    public Recipe incrementView(Long id) {
        Assert.notNull(id, "食谱ID不能为空");
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食谱不存在，ID：" + id));

        recipe.setViews(recipe.getViews() + 1);
        return recipeRepository.save(recipe);
    }

    // ========== 兼容旧接口（如需保留原无权限参数的方法） ==========
    /**
     * 兼容旧版getRecipeById（无当前用户ID）- 仅查公开食谱
     * @deprecated 建议使用带currentUserId的重载方法
     */
    @Deprecated
    public Recipe getRecipeById(Long id) {
        return getRecipeById(id, -1L); // 传入无效用户ID，仅能查公开食谱
    }

    /**
     * 兼容旧版updateRecipe（无管理员标识）
     * @deprecated 建议使用带isAdmin的重载方法
     */
    @Deprecated
    public Recipe updateRecipe(Long id, Recipe recipe, Long userId) {
        Boolean isAdmin = userRepository.isAdmin(userId);
        return updateRecipe(id, recipe, userId, isAdmin);
    }

    /**
     * 兼容旧版deleteRecipe（无管理员标识）
     * @deprecated 建议使用带isAdmin的重载方法
     */
    @Deprecated
    public void deleteRecipe(Long id, Long userId) {
        Boolean isAdmin = userRepository.isAdmin(userId);
        deleteRecipe(id, userId, isAdmin);
    }
}
