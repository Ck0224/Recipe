package com.recipe.controller;

import com.recipe.common.Result;
import com.recipe.dto.RecipeDTO;
import com.recipe.entity.Ingredient;
import com.recipe.entity.Recipe;
import com.recipe.entity.Step;
import com.recipe.entity.User;
import com.recipe.service.RecipeService;
import com.recipe.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 食谱控制器（完整适配：权限控制 + ID+分类+菜名+食材+难度 多条件搜索）
 */
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;
    private final UserService userService; // 新增：注入用户服务用于权限判断

    // ========== 基础CRUD接口（增强权限控制） ==========
    // 1. 创建食谱（原有逻辑不变）
    @PostMapping
    public ResponseEntity<Result<RecipeDTO>> createRecipe(
            @RequestBody Recipe recipe,
            @RequestParam Long userId
    ) {
        try {
            Recipe createdRecipe = recipeService.createRecipe(recipe, userId);
            RecipeDTO dto = convertToDTO(createdRecipe);
            return ResponseEntity.ok(Result.success(dto));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("创建食谱失败：" + e.getMessage()));
        }
    }

    // 2. 根据ID查询食谱（新增：当前用户ID参数，权限校验）
    @GetMapping("/{id}")
    public ResponseEntity<Result<RecipeDTO>> getRecipeById(
            @PathVariable Long id,
            @RequestParam Long currentUserId // 新增：当前登录用户ID
    ) {
        try {
            Recipe recipe = recipeService.getRecipeById(id, currentUserId);
            RecipeDTO dto = convertToDTO(recipe);
            return ResponseEntity.ok(Result.success(dto));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("查询食谱失败：" + e.getMessage()));
        }
    }

    // 3. 更新食谱（新增：管理员权限判断）
    @PutMapping("/{id}")
    public ResponseEntity<Result<RecipeDTO>> updateRecipe(
            @PathVariable Long id,
            @RequestBody Recipe recipe,
            @RequestParam Long currentUserId // 当前登录用户ID
    ) {
        try {
            // 判断当前用户是否为管理员
            Boolean isAdmin = userService.isAdmin(currentUserId);
            // 调用带权限的更新方法
            Recipe updatedRecipe = recipeService.updateRecipe(id, recipe, currentUserId, isAdmin);
            RecipeDTO dto = convertToDTO(updatedRecipe);
            return ResponseEntity.ok(Result.success(dto));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("更新食谱失败：" + e.getMessage()));
        }
    }

    // 4. 删除食谱（新增：管理员权限判断）
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteRecipe(
            @PathVariable Long id,
            @RequestParam Long currentUserId // 当前登录用户ID
    ) {
        try {
            // 判断当前用户是否为管理员
            Boolean isAdmin = userService.isAdmin(currentUserId);
            // 调用带权限的删除方法
            recipeService.deleteRecipe(id, currentUserId, isAdmin);
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("删除食谱失败：" + e.getMessage()));
        }
    }

    // ========== 核心新增：用户可见食谱查询（公开 + 自己的私有） ==========
    @GetMapping("/visible")
    public ResponseEntity<Result<Page<RecipeDTO>>> getUserVisibleRecipes(
            @RequestParam Long currentUserId,     // 当前登录用户ID
            @RequestParam(required = false) Long id,          // 食谱ID
            @RequestParam(required = false) String title,     // 菜名
            @RequestParam(required = false) String category,  // 分类
            @RequestParam(required = false) String difficulty,// 难度
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            // 调用用户可见食谱查询
            Page<Recipe> recipePage = recipeService.getUserVisibleRecipes(
                    currentUserId, id, title, category, difficulty, pageable);
            Page<RecipeDTO> dtoPage = recipePage.map(this::convertToDTO);
            return ResponseEntity.ok(Result.success(dtoPage));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("查询可见食谱失败：" + e.getMessage()));
        }
    }

    // ========== 核心增强：带权限的多条件查询（管理员看所有，普通用户看可见） ==========
    @GetMapping("/search/all")
    public ResponseEntity<Result<Page<RecipeDTO>>> getRecipesWithPermission(
            @RequestParam Long currentUserId,     // 当前登录用户ID
            @RequestParam(required = false) Long id,          // 食谱ID
            @RequestParam(required = false) String title,     // 菜名
            @RequestParam(required = false) String category,  // 分类
            @RequestParam(required = false) String ingredient,// 食材
            @RequestParam(required = false) String difficulty,// 难度
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            // 判断当前用户是否为管理员
            Boolean isAdmin = userService.isAdmin(currentUserId);
            // 调用带权限的多条件查询
            Page<Recipe> recipePage = recipeService.getRecipesByMultiConditionsWithPermission(
                    currentUserId, isAdmin, id, title, category, ingredient, difficulty, pageable);
            Page<RecipeDTO> dtoPage = recipePage.map(this::convertToDTO);
            return ResponseEntity.ok(Result.success(dtoPage));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("查询食谱失败：" + e.getMessage()));
        }
    }

    // ========== 保留：我的食谱查询（按用户ID） ==========
    @GetMapping("/my")
    public ResponseEntity<Result<Page<RecipeDTO>>> getMyRecipes(
            @RequestParam Long userId,            // 当前登录用户ID
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Recipe> recipePage = recipeService.getRecipesByUserId(userId, pageable);
            Page<RecipeDTO> dtoPage = recipePage.map(this::convertToDTO);
            return ResponseEntity.ok(Result.success(dtoPage));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("查询我的食谱失败：" + e.getMessage()));
        }
    }

    // ========== 兼容原有：仅查公开食谱的多条件查询（新增私有食谱过滤） ==========
    @GetMapping
    public ResponseEntity<Result<Page<RecipeDTO>>> getPublicRecipes(
            @RequestParam(required = false) Long id,          // 食谱ID（正整数）
            @RequestParam(required = false) String title,     // 菜名（核心新增）
            @RequestParam(required = false) String category,  // 分类
            @RequestParam(required = false) String ingredient,// 食材
            @RequestParam(required = false) String difficulty,// 难度（String适配前端）
            @RequestParam(required = false) Long userId,      // 新增：当前登录用户ID（前端传递）
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Recipe> recipePage;

            // 核心修改：添加私有食谱权限过滤
            if (userId != null) {
                // 1. 普通用户：查询「公开食谱」+「自己的私有食谱」
                recipePage = recipeService.getUserVisibleRecipes(
                        userId, id, title, category, difficulty, pageable);
            } else {
                // 2. 无用户ID（管理员/未登录）：仅查公开食谱
                recipePage = recipeService.getRecipesByMultiConditions(
                        id, title, category, ingredient, difficulty, pageable);
            }

            Page<RecipeDTO> dtoPage = recipePage.map(this::convertToDTO);
            return ResponseEntity.ok(Result.success(dtoPage));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("查询食谱失败：" + e.getMessage()));
        }
    }

    // ========== 工具方法：DTO转换（保留原有逻辑） ==========
    private RecipeDTO convertToDTO(Recipe recipe) {
        RecipeDTO dto = new RecipeDTO();

        // 1. 基础字段
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setCoverImage(recipe.getCoverImage());
        dto.setPrepTime(recipe.getPrepTime());
        dto.setCookTime(recipe.getCookTime());
        dto.setServings(recipe.getServings());
        dto.setDifficulty(recipe.getDifficulty().name()); // 枚举转字符串
        dto.setCategory(recipe.getCategory());
        dto.setTagList(recipe.getTagList());
        dto.setIsPrivate(recipe.getIsPrivate());
        dto.setViews(recipe.getViews());
        dto.setLikes(recipe.getLikes());
        dto.setCreatedAt(recipe.getCreatedAt());
        dto.setUpdatedAt(recipe.getUpdatedAt());

        // 2. 用户信息
        if (recipe.getUser() != null) {
            RecipeDTO.UserSimpleDTO userDTO = new RecipeDTO.UserSimpleDTO();
            User user = recipe.getUser();
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setEmail(user.getEmail());
            userDTO.setAvatarUrl(user.getAvatarUrl());
            userDTO.setIsAdmin(user.getIsAdmin()); // 新增：返回管理员标识
            dto.setUser(userDTO);
        }

        // 3. 食材列表
        if (recipe.getIngredients() != null) {
            List<RecipeDTO.IngredientDTO> ingredientDTOS = recipe.getIngredients().stream()
                    .map(this::convertIngredientToDTO)
                    .collect(Collectors.toList());
            dto.setIngredients(ingredientDTOS);
        }

        // 4. 步骤列表
        if (recipe.getSteps() != null) {
            List<RecipeDTO.StepDTO> stepDTOS = recipe.getSteps().stream()
                    .map(this::convertStepToDTO)
                    .collect(Collectors.toList());
            dto.setSteps(stepDTOS);
        }

        return dto;
    }

    // 食材实体转DTO
    private RecipeDTO.IngredientDTO convertIngredientToDTO(Ingredient ingredient) {
        RecipeDTO.IngredientDTO dto = new RecipeDTO.IngredientDTO();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        dto.setQuantity(ingredient.getQuantity());
        dto.setUnit(ingredient.getUnit());
        dto.setNote(ingredient.getNote());
        dto.setSortOrder(ingredient.getSortOrder());
        return dto;
    }

    // 步骤实体转DTO
    private RecipeDTO.StepDTO convertStepToDTO(Step step) {
        RecipeDTO.StepDTO dto = new RecipeDTO.StepDTO();
        dto.setId(step.getId());
        dto.setStepNumber(step.getStepNumber());
        dto.setDescription(step.getDescription());
        dto.setImageUrl(step.getImageUrl());
        dto.setTimerMinutes(step.getTimerMinutes());
        dto.setSortOrder(step.getSortOrder());
        return dto;
    }

    // ========== 兼容旧版接口（无currentUserId参数） ==========
    /**
     * 兼容旧版查询食谱详情（仅查公开食谱）
     * @deprecated 建议使用带currentUserId的重载方法
     */
    @Deprecated
    @GetMapping("/{id}/public")
    public ResponseEntity<Result<RecipeDTO>> getPublicRecipeById(@PathVariable Long id) {
        return getRecipeById(id, -1L); // 传入无效用户ID，仅查公开食谱
    }
}
