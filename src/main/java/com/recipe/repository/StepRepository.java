package com.recipe.repository;

import com.recipe.entity.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {
    // 根据食谱ID删除步骤
    @Modifying
    @Query("DELETE FROM Step s WHERE s.recipe.id = :recipeId")
    void deleteByRecipeId(@Param("recipeId") Long recipeId);
}
