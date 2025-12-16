package com.recipe.repository;

import com.recipe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // ========== 原有基础查询（保留） ==========
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // ========== 新增：管理员相关查询 ==========
    // 查询所有管理员用户
    List<User> findByIsAdminTrue();

    // 判断用户是否为管理员
    @Query("SELECT u.isAdmin FROM User u WHERE u.id = :userId")
    Boolean isAdmin(@Param("userId") Long userId);

    // ========== 新增：用户信息更新（密码/头像） ==========
    // 修改密码（无需加载整个用户对象，性能更优）
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :newPassword WHERE u.id = :userId")
    int updatePassword(@Param("userId") Long userId, @Param("newPassword") String newPassword);

    // 修改头像URL
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.avatarUrl = :avatarUrl WHERE u.id = :userId")
    int updateAvatarUrl(@Param("userId") Long userId, @Param("avatarUrl") String avatarUrl);

    // ========== 新增：管理员面板 - 分页查询所有用户 ==========
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findAllUsers(/* 支持分页：配合Pageable使用 */);

    // 分页查询所有用户（含管理员筛选）
    @Query("SELECT u FROM User u WHERE (:isAdmin IS NULL OR u.isAdmin = :isAdmin) ORDER BY u.createdAt DESC")
    List<User> findUsersByAdminStatus(@Param("isAdmin") Boolean isAdmin);
}
