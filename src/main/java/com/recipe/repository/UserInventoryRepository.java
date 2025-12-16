package com.recipe.repository;

import com.recipe.entity.UserInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInventoryRepository extends JpaRepository<UserInventory, Long> {
    // 按用户ID查询库存（对应代码中的findByUserId方法）
    List<UserInventory> findByUserId(Long userId);
}
