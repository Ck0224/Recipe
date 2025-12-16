package com.recipe.service.impl;

import com.recipe.entity.Ingredient;
import com.recipe.entity.Recipe;
import com.recipe.entity.User;
import com.recipe.entity.UserInventory;
import com.recipe.repository.IngredientRepository;
import com.recipe.repository.RecipeRepository;
import com.recipe.repository.UserInventoryRepository;
import com.recipe.repository.UserRepository;
import com.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 食谱业务实现类
 */
@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {
    // 注入JPA仓库
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final IngredientRepository ingredientRepository;

    /**
     * 创建食谱（关联用户+级联保存食材/步骤）
     */
    @Override
    @Transactional
    public Recipe createRecipe(Recipe recipe, Long userId) {
        // 1. 查询用户并关联
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        recipe.setUser(user);

        // 2. 关联食材/步骤与食谱（级联保存）
        if (recipe.getIngredients() != null) {
            recipe.getIngredients().forEach(ingredient -> ingredient.setRecipe(recipe));
        }
        if (recipe.getSteps() != null) {
            recipe.getSteps().forEach(step -> step.setRecipe(recipe));
        }

        // 3. 保存食谱（自动级联保存食材/步骤）
        return recipeRepository.save(recipe);
    }

    /**
     * 根据ID查询食谱（含食材+步骤）
     */
    @Override
    @Transactional(readOnly = true)
    public Recipe getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食谱不存在"));

        // 非公开食谱仅作者可看
        if (recipe.getIsPrivate()) {
            // 实际需结合Security上下文获取当前登录用户ID，此处简化
            // Long currentUserId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            // if (!recipe.getUser().getId().equals(currentUserId)) {
            //     throw new RuntimeException("无权限查看私有食谱");
            // }
        }

        // 浏览量+1
        incrementView(id);
        return recipe;
    }

    /**
     * 分页查询公开食谱
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Recipe> getPublicRecipes(Pageable pageable) {
        return recipeRepository.findByIsPrivateFalse(pageable);
    }

    /**
     * 按分类查询公开食谱
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Recipe> getRecipesByCategory(String category, Pageable pageable) {
        return recipeRepository.findByCategoryAndIsPrivateFalse(category, pageable);
    }

    /**
     * 按难度查询公开食谱
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Recipe> getRecipesByDifficulty(Recipe.Difficulty difficulty, Pageable pageable) {
        return recipeRepository.findByDifficultyAndIsPrivateFalse(difficulty, pageable);
    }

    /**
     * 按用户ID查询个人食谱
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Recipe> getRecipesByUserId(Long userId, Pageable pageable) {
        return recipeRepository.findByUserId(userId, pageable);
    }

    /**
     * 关键词搜索食谱
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Recipe> searchRecipes(String keyword, Pageable pageable) {
        return recipeRepository.searchPublicRecipes(keyword, pageable);
    }

    /**
     * 更新食谱（仅作者可更）
     */
    @Override
    @Transactional
    public Recipe updateRecipe(Long id, Recipe updateRecipe, Long userId) {
        // 1. 查询原食谱
        Recipe originalRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食谱不存在"));

        // 2. 验证权限（仅作者可更）
        if (!originalRecipe.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限修改该食谱");
        }

        // 3. 更新基础字段
        if (updateRecipe.getTitle() != null) {
            originalRecipe.setTitle(updateRecipe.getTitle());
        }
        if (updateRecipe.getDescription() != null) {
            originalRecipe.setDescription(updateRecipe.getDescription());
        }
        if (updateRecipe.getCoverImage() != null) {
            originalRecipe.setCoverImage(updateRecipe.getCoverImage());
        }
        if (updateRecipe.getPrepTime() != null) {
            originalRecipe.setPrepTime(updateRecipe.getPrepTime());
        }
        if (updateRecipe.getCookTime() != null) {
            originalRecipe.setCookTime(updateRecipe.getCookTime());
        }
        if (updateRecipe.getServings() != null) {
            originalRecipe.setServings(updateRecipe.getServings());
        }
        if (updateRecipe.getDifficulty() != null) {
            originalRecipe.setDifficulty(updateRecipe.getDifficulty());
        }
        if (updateRecipe.getCategory() != null) {
            originalRecipe.setCategory(updateRecipe.getCategory());
        }
        if (updateRecipe.getTags() != null) {
            originalRecipe.setTags(updateRecipe.getTags());
        }
        if (updateRecipe.getIsPrivate() != null) {
            originalRecipe.setIsPrivate(updateRecipe.getIsPrivate());
        }

        // 4. 更新食材（先删旧的，再保存新的）
        if (updateRecipe.getIngredients() != null) {
            ingredientRepository.deleteByRecipeId(id); // 删除旧食材
            updateRecipe.getIngredients().forEach(ingredient -> {
                ingredient.setRecipe(originalRecipe);
                ingredientRepository.save(ingredient);
            });
            originalRecipe.setIngredients(updateRecipe.getIngredients());
        }

        // 5. 更新步骤（同理）
        if (updateRecipe.getSteps() != null) {
            // 需实现StepRepository的deleteByRecipeId方法
            // stepRepository.deleteByRecipeId(id);
            updateRecipe.getSteps().forEach(step -> {
                step.setRecipe(originalRecipe);
                // stepRepository.save(step);
            });
            originalRecipe.setSteps(updateRecipe.getSteps());
        }

        // 6. 保存更新
        return recipeRepository.save(originalRecipe);
    }

    /**
     * 删除食谱（仅作者可删）
     */
    @Override
    @Transactional
    public void deleteRecipe(Long id, Long userId) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食谱不存在"));

        // 验证权限
        if (!recipe.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限删除该食谱");
        }

        // 删除食谱（级联删除食材/步骤）
        recipeRepository.delete(recipe);
    }

    /**
     * 根据用户库存食材推荐食谱
     */
    @Override
    @Transactional(readOnly = true)
    public List<Recipe> recommendRecipesByInventory(Long userId) {
        // 1. 获取用户库存食材列表
        List<UserInventory> inventoryList = userInventoryRepository.findByUserId(userId);
        if (inventoryList.isEmpty()) {
            throw new RuntimeException("用户暂无库存食材，无法推荐");
        }

        // 2. 提取食材名称
        List<String> ingredientNames = inventoryList.stream()
                .map(UserInventory::getIngredientName)
                .collect(Collectors.toList());

        // 3. 根据食材匹配食谱（按匹配度排序）
        return recipeRepository.findRecipesByIngredients(ingredientNames);
    }

    /**
     * 食谱点赞
     */
    @Override
    @Transactional
    public Recipe likeRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食谱不存在"));
        recipe.setLikes(recipe.getLikes() + 1);
        return recipeRepository.save(recipe);
    }

    /**
     * 浏览量+1
     */
    @Override
    @Transactional
    public Recipe incrementView(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食谱不存在"));
        recipe.setViews(recipe.getViews() + 1);
        return recipeRepository.save(recipe);
    }
}
