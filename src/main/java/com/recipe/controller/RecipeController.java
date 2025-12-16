package com.recipe.controller;

import com.recipe.common.Result;
import com.recipe.dto.RecipeDTO;
import com.recipe.entity.Ingredient;
import com.recipe.entity.Recipe;
import com.recipe.entity.Step;
import com.recipe.entity.User;
import com.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 食谱控制器（完整适配：ID+分类+菜名+食材+难度 多条件搜索）
 */
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    // ========== 基础CRUD接口（保留原有逻辑） ==========
    // 创建食谱
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

    // 根据ID查询食谱
    @GetMapping("/{id}")
    public ResponseEntity<Result<RecipeDTO>> getRecipeById(@PathVariable Long id) {
        try {
            Recipe recipe = recipeService.getRecipeById(id);
            RecipeDTO dto = convertToDTO(recipe);
            return ResponseEntity.ok(Result.success(dto));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("查询食谱失败：" + e.getMessage()));
        }
    }

    // 更新食谱
    @PutMapping("/{id}")
    public ResponseEntity<Result<RecipeDTO>> updateRecipe(
            @PathVariable Long id,
            @RequestBody Recipe recipe,
            @RequestParam Long userId
    ) {
        try {
            Recipe updatedRecipe = recipeService.updateRecipe(id, recipe, userId);
            RecipeDTO dto = convertToDTO(updatedRecipe);
            return ResponseEntity.ok(Result.success(dto));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("更新食谱失败：" + e.getMessage()));
        }
    }

    // 删除食谱
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteRecipe(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        try {
            recipeService.deleteRecipe(id, userId);
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("删除食谱失败：" + e.getMessage()));
        }
    }

    // ========== 核心：多条件组合查询（ID+分类+菜名+食材+难度） ==========
    @GetMapping
    public ResponseEntity<Result<Page<RecipeDTO>>> getPublicRecipes(
            @RequestParam(required = false) Long id,          // 食谱ID（正整数）
            @RequestParam(required = false) String title,     // 菜名（核心新增）
            @RequestParam(required = false) String category,  // 分类
            @RequestParam(required = false) String ingredient,// 食材
            @RequestParam(required = false) String difficulty,// 难度（String适配前端）
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 1. 分页条件（和原有逻辑一致）
            Pageable pageable = PageRequest.of(page, size);

            // 2. 调用多条件查询Service（新增title参数）
            Page<Recipe> recipePage = recipeService.getRecipesByMultiConditions(
                    id, title, category, ingredient, difficulty, pageable);

            // 3. DTO转换（复用原有逻辑）
            Page<RecipeDTO> dtoPage = recipePage.map(this::convertToDTO);

            return ResponseEntity.ok(Result.success(dtoPage));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("查询食谱失败：" + e.getMessage()));
        }
    }

    // ========== 废弃冗余接口（原有/search接口可删除，功能已合并到上面的多条件查询） ==========
    // 若需兼容旧版前端，可保留此接口并内部调用多条件查询
    /*
    @GetMapping("/search")
    public ResponseEntity<Result<Page<RecipeDTO>>> searchRecipes(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            // 复用多条件查询，仅传title参数（等效原有关键词搜索）
            Page<Recipe> recipePage = recipeService.getRecipesByMultiConditions(
                    null, keyword, null, null, null, pageable);
            Page<RecipeDTO> dtoPage = recipePage.map(this::convertToDTO);
            return ResponseEntity.ok(Result.success(dtoPage));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("搜索食谱失败：" + e.getMessage()));
        }
    }
    */

    // ========== DTO转换方法（保留原有逻辑，无需修改） ==========
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
}
