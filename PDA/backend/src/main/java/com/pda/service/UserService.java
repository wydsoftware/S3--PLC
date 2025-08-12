package com.pda.service;

import com.pda.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户服务接口
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
public interface UserService {

    /**
     * 保存用户
     */
    User saveUser(User user);

    /**
     * 根据ID查找用户
     */
    Optional<User> findById(Long id);

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
     * 用户登录验证
     */
    Optional<User> login(String username, String password);

    /**
     * 创建新用户
     */
    User createUser(String username, String password, String realName, String role);

    /**
     * 更新用户信息
     */
    User updateUser(Long id, String realName, String role, String status);

    /**
     * 更新用户密码
     */
    boolean updatePassword(Long id, String oldPassword, String newPassword);

    /**
     * 重置用户密码
     */
    boolean resetPassword(Long id, String newPassword);

    /**
     * 删除用户
     */
    boolean deleteUser(Long id);

    /**
     * 启用用户
     */
    boolean enableUser(Long id);

    /**
     * 禁用用户
     */
    boolean disableUser(Long id);

    /**
     * 获取所有用户
     */
    List<User> findAllUsers();

    /**
     * 根据角色查找用户
     */
    List<User> findByRole(String role);

    /**
     * 根据状态查找用户
     */
    List<User> findByStatus(String status);

    /**
     * 根据角色和状态查找用户
     */
    List<User> findByRoleAndStatus(String role, String status);

    /**
     * 分页查询用户
     */
    Page<User> findUsersWithConditions(String username, String role, String status, Pageable pageable);

    /**
     * 搜索用户
     */
    List<User> searchUsers(String keyword);

    /**
     * 分页搜索用户
     */
    Page<User> searchUsers(String keyword, Pageable pageable);

    /**
     * 获取用户统计信息
     */
    Map<String, Long> getUserStatistics();

    /**
     * 获取用户角色分布
     */
    Map<String, Long> getUserRoleDistribution();

    /**
     * 获取用户状态分布
     */
    Map<String, Long> getUserStatusDistribution();

    /**
     * 获取最近创建的用户
     */
    List<User> getRecentUsers(int limit);

    /**
     * 获取最近更新的用户
     */
    List<User> getRecentUpdatedUsers(int limit);

    /**
     * 根据时间范围查询用户
     */
    List<User> findUsersByDateRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取月度用户创建统计
     */
    Map<String, Long> getMonthlyUserCreationStats(int months);

    /**
     * 批量更新用户状态
     */
    int batchUpdateUserStatus(List<Long> userIds, String status);

    /**
     * 批量删除用户
     */
    int batchDeleteUsers(List<Long> userIds);

    /**
     * 检查是否存在启用的管理员
     */
    boolean hasEnabledAdmin();

    /**
     * 获取默认管理员用户
     */
    Optional<User> getDefaultAdmin();

    /**
     * 验证用户权限
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 检查用户是否为管理员
     */
    boolean isAdmin(Long userId);

    /**
     * 检查用户是否启用
     */
    boolean isUserEnabled(Long userId);

    /**
     * 更新用户最后登录时间
     */
    void updateLastLoginTime(Long userId);

    /**
     * 获取在线用户数量
     */
    long getOnlineUserCount();

    /**
     * 验证密码强度
     */
    boolean validatePasswordStrength(String password);

    /**
     * 生成随机密码
     */
    String generateRandomPassword();

    /**
     * 检查用户名格式
     */
    boolean validateUsernameFormat(String username);

    /**
     * 初始化默认用户
     */
    void initializeDefaultUsers();
}