package com.pda.repository;

import com.pda.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层接口
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据用户名查找用户（忽略大小写）
     */
    Optional<User> findByUsernameIgnoreCase(String username);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查用户名是否存在（忽略大小写）
     */
    boolean existsByUsernameIgnoreCase(String username);

    /**
     * 根据角色查找用户列表
     */
    List<User> findByRole(String role);

    /**
     * 根据状态查找用户列表
     */
    List<User> findByStatus(String status);

    /**
     * 根据角色和状态查找用户列表
     */
    List<User> findByRoleAndStatus(String role, String status);

    /**
     * 查找启用的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.status = 'enabled'")
    List<User> findEnabledUsers();

    /**
     * 查找管理员用户列表
     */
    @Query("SELECT u FROM User u WHERE u.role = 'admin' AND u.status = 'enabled'")
    List<User> findEnabledAdminUsers();

    /**
     * 根据用户名模糊查询
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username%")
    List<User> findByUsernameLike(@Param("username") String username);

    /**
     * 分页查询用户
     */
    @Query("SELECT u FROM User u WHERE (:username IS NULL OR u.username LIKE %:username%) " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "ORDER BY u.createdTime DESC")
    Page<User> findUsersWithConditions(@Param("username") String username,
                                      @Param("role") String role,
                                      @Param("status") String status,
                                      Pageable pageable);

    /**
     * 统计用户数量
     */
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

    /**
     * 统计启用用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'enabled'")
    long countEnabledUsers();

    /**
     * 统计管理员用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'admin' AND u.status = 'enabled'")
    long countEnabledAdminUsers();

    /**
     * 根据角色统计用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") String role);

    /**
     * 根据状态统计用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") String status);

    /**
     * 查找指定时间范围内创建的用户
     */
    @Query("SELECT u FROM User u WHERE u.createdTime BETWEEN :startTime AND :endTime ORDER BY u.createdTime DESC")
    List<User> findUsersCreatedBetween(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 查找最近创建的用户
     */
    @Query("SELECT u FROM User u ORDER BY u.createdTime DESC")
    List<User> findRecentUsers(Pageable pageable);

    /**
     * 查找最近更新的用户
     */
    @Query("SELECT u FROM User u ORDER BY u.updatedTime DESC")
    List<User> findRecentlyUpdatedUsers(Pageable pageable);

    /**
     * 批量更新用户状态
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :status, u.updatedTime = CURRENT_TIMESTAMP WHERE u.id IN :ids")
    int updateUserStatusBatch(@Param("ids") List<Long> ids, @Param("status") String status);

    /**
     * 批量删除用户
     */
    @Modifying
    @Query("DELETE FROM User u WHERE u.id IN :ids")
    int deleteUsersBatch(@Param("ids") List<Long> ids);

    /**
     * 更新用户密码
     */
    @Modifying
    @Query("UPDATE User u SET u.password = :password, u.updatedTime = CURRENT_TIMESTAMP WHERE u.id = :id")
    int updateUserPassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 更新用户角色
     */
    @Modifying
    @Query("UPDATE User u SET u.role = :role, u.updatedTime = CURRENT_TIMESTAMP WHERE u.id = :id")
    int updateUserRole(@Param("id") Long id, @Param("role") String role);

    /**
     * 启用用户
     */
    @Modifying
    @Query("UPDATE User u SET u.status = 'enabled', u.updatedTime = CURRENT_TIMESTAMP WHERE u.id = :id")
    int enableUser(@Param("id") Long id);

    /**
     * 禁用用户
     */
    @Modifying
    @Query("UPDATE User u SET u.status = 'disabled', u.updatedTime = CURRENT_TIMESTAMP WHERE u.id = :id")
    int disableUser(@Param("id") Long id);

    /**
     * 检查除指定ID外用户名是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :id")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("id") Long id);

    /**
     * 获取用户角色统计
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserRoleStatistics();

    /**
     * 获取用户状态统计
     */
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> getUserStatusStatistics();

    /**
     * 获取用户创建时间统计（按月）
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', u.createdTime, '%Y-%m') as month, COUNT(u) " +
           "FROM User u " +
           "WHERE u.createdTime >= :startTime " +
           "GROUP BY FUNCTION('DATE_FORMAT', u.createdTime, '%Y-%m') " +
           "ORDER BY month DESC")
    List<Object[]> getUserCreationStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 查找用户名包含指定关键字的用户
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.realName LIKE %:keyword%")
    List<User> searchUsersByKeyword(@Param("keyword") String keyword);

    /**
     * 分页搜索用户
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.realName LIKE %:keyword% ORDER BY u.createdTime DESC")
    Page<User> searchUsersByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 检查是否存在管理员用户
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.role = 'admin' AND u.status = 'enabled'")
    boolean hasEnabledAdminUser();

    /**
     * 获取默认管理员用户
     */
    @Query("SELECT u FROM User u WHERE u.username = 'admin' AND u.role = 'admin'")
    Optional<User> findDefaultAdminUser();
}