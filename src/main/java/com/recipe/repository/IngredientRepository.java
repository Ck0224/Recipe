package com.recipe.repository;

import com.recipe.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    // 根据食谱ID删除食材
    @Modifying
    @Query("DELETE FROM Ingredient i WHERE i.recipe.id = :recipeId")
    void deleteByRecipeId(@Param("recipeId") Long recipeId);
}
