package com.recipe.repository;

import com.recipe.entity.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {
    // ========== 仅新增以下查询方法 ==========
    List<Step> findByRecipeIdOrderByStepNumberAsc(Long recipeId);

    // 原有删除方法保留
    @Modifying
    @Query("DELETE FROM Step s WHERE s.recipe.id = :recipeId")
    void deleteByRecipeId(@Param("recipeId") Long recipeId);
}
