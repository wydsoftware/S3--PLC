package com.pda.service.impl;

import com.pda.entity.User;
import com.pda.repository.UserRepository;
import com.pda.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // 用户名格式验证正则表达式
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    
    // 密码强度验证正则表达式（至少8位，包含字母和数字）
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$");
    
    // 随机密码字符集
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public User saveUser(User user) {
        if (user.getId() == null) {
            // 新用户，加密密码
            if (StringUtils.hasText(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            user.setCreatedTime(LocalDateTime.now());
        }
        user.setUpdatedTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByUsernameIgnoreCase(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if ("enabled".equals(user.getStatus()) && passwordEncoder.matches(password, user.getPassword())) {
                // 更新最后登录时间
                updateLastLoginTime(user.getId());
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public User createUser(String username, String password, String realName, String role) {
        if (existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        
        if (!validateUsernameFormat(username)) {
            throw new IllegalArgumentException("用户名格式不正确");
        }
        
        if (!validatePasswordStrength(password)) {
            throw new IllegalArgumentException("密码强度不够");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // saveUser方法会自动加密
        user.setRealName(realName);
        user.setRole(role);
        user.setStatus("enabled");
        
        return saveUser(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, String realName, String role, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + id));
        
        if (StringUtils.hasText(realName)) {
            user.setRealName(realName);
        }
        if (StringUtils.hasText(role)) {
            user.setRole(role);
        }
        if (StringUtils.hasText(status)) {
            user.setStatus(status);
        }
        user.setUpdatedTime(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return false;
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        if (!validatePasswordStrength(newPassword)) {
            throw new IllegalArgumentException("新密码强度不够");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedTime(LocalDateTime.now());
        userRepository.save(user);
        
        return true;
    }

    @Override
    @Transactional
    public boolean resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return false;
        }
        
        if (!validatePasswordStrength(newPassword)) {
            throw new IllegalArgumentException("新密码强度不够");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedTime(LocalDateTime.now());
        userRepository.save(user);
        
        return true;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        
        // 检查是否为最后一个管理员
        User user = userRepository.findById(id).orElse(null);
        if (user != null && "admin".equals(user.getRole())) {
            long adminCount = userRepository.countByRoleAndStatus("admin", "enabled");
            if (adminCount <= 1) {
                throw new IllegalStateException("不能删除最后一个管理员用户");
            }
        }
        
        userRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public boolean enableUser(Long id) {
        return userRepository.updateUserStatus(id, "enabled") > 0;
    }

    @Override
    @Transactional
    public boolean disableUser(Long id) {
        // 检查是否为最后一个管理员
        User user = userRepository.findById(id).orElse(null);
        if (user != null && "admin".equals(user.getRole()) && "enabled".equals(user.getStatus())) {
            long enabledAdminCount = userRepository.countByRoleAndStatus("admin", "enabled");
            if (enabledAdminCount <= 1) {
                throw new IllegalStateException("不能禁用最后一个启用的管理员用户");
            }
        }
        
        return userRepository.updateUserStatus(id, "disabled") > 0;
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findByRole(String role) {
        return userRepository.findByRole(role);
    }

    @Override
    public List<User> findByStatus(String status) {
        return userRepository.findByStatus(status);
    }

    @Override
    public List<User> findByRoleAndStatus(String role, String status) {
        return userRepository.findByRoleAndStatus(role, status);
    }

    @Override
    public Page<User> findUsersWithConditions(String username, String role, String status, Pageable pageable) {
        return userRepository.findUsersWithConditions(username, role, status, pageable);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsersByKeyword(keyword);
    }

    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsersByKeyword(keyword, pageable);
    }

    @Override
    public Map<String, Long> getUserStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", userRepository.countAllUsers());
        stats.put("enabled", userRepository.countByStatus("enabled"));
        stats.put("disabled", userRepository.countByStatus("disabled"));
        stats.put("admin", userRepository.countByRole("admin"));
        stats.put("user", userRepository.countByRole("user"));
        return stats;
    }

    @Override
    public Map<String, Long> getUserRoleDistribution() {
        List<Object[]> results = userRepository.getUserRoleDistribution();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public Map<String, Long> getUserStatusDistribution() {
        List<Object[]> results = userRepository.getUserStatusDistribution();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public List<User> getRecentUsers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return userRepository.findRecentUsers(pageable);
    }

    @Override
    public List<User> getRecentUpdatedUsers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return userRepository.findRecentUpdatedUsers(pageable);
    }

    @Override
    public List<User> findUsersByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return userRepository.findUsersByCreatedTimeBetween(startTime, endTime);
    }

    @Override
    public Map<String, Long> getMonthlyUserCreationStats(int months) {
        LocalDateTime startTime = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = userRepository.getMonthlyUserCreationStats(startTime);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1],
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    @Override
    @Transactional
    public int batchUpdateUserStatus(List<Long> userIds, String status) {
        // 检查是否会禁用所有管理员
        if ("disabled".equals(status)) {
            List<User> users = userRepository.findAllById(userIds);
            long adminCount = users.stream()
                    .filter(user -> "admin".equals(user.getRole()))
                    .count();
            
            long totalEnabledAdmins = userRepository.countByRoleAndStatus("admin", "enabled");
            if (adminCount >= totalEnabledAdmins) {
                throw new IllegalStateException("不能禁用所有管理员用户");
            }
        }
        
        return userRepository.batchUpdateUserStatus(userIds, status);
    }

    @Override
    @Transactional
    public int batchDeleteUsers(List<Long> userIds) {
        // 检查是否会删除所有管理员
        List<User> users = userRepository.findAllById(userIds);
        long adminCount = users.stream()
                .filter(user -> "admin".equals(user.getRole()))
                .count();
        
        long totalAdmins = userRepository.countByRole("admin");
        if (adminCount >= totalAdmins) {
            throw new IllegalStateException("不能删除所有管理员用户");
        }
        
        return userRepository.batchDeleteUsers(userIds);
    }

    @Override
    public boolean hasEnabledAdmin() {
        return userRepository.hasEnabledAdmin();
    }

    @Override
    public Optional<User> getDefaultAdmin() {
        return userRepository.getDefaultAdmin();
    }

    @Override
    public boolean hasPermission(Long userId, String permission) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !"enabled".equals(user.getStatus())) {
            return false;
        }
        
        // 管理员拥有所有权限
        if ("admin".equals(user.getRole())) {
            return true;
        }
        
        // 普通用户权限检查（可根据需要扩展）
        return "user".equals(user.getRole()) && "read".equals(permission);
    }

    @Override
    public boolean isAdmin(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && "admin".equals(user.getRole());
    }

    @Override
    public boolean isUserEnabled(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && "enabled".equals(user.getStatus());
    }

    @Override
    @Transactional
    public void updateLastLoginTime(Long userId) {
        userRepository.updateLastLoginTime(userId, LocalDateTime.now());
    }

    @Override
    public long getOnlineUserCount() {
        // 简单实现：返回启用用户数量
        // 实际项目中可能需要维护在线用户会话
        return userRepository.countByStatus("enabled");
    }

    @Override
    public boolean validatePasswordStrength(String password) {
        return StringUtils.hasText(password) && PASSWORD_PATTERN.matcher(password).matches();
    }

    @Override
    public String generateRandomPassword() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }

    @Override
    public boolean validateUsernameFormat(String username) {
        return StringUtils.hasText(username) && USERNAME_PATTERN.matcher(username).matches();
    }

    @Override
    @PostConstruct
    @Transactional
    public void initializeDefaultUsers() {
        // 检查是否已存在管理员用户
        if (!hasEnabledAdmin()) {
            log.info("初始化默认管理员用户...");
            
            // 创建默认管理员
            if (!existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setRealName("系统管理员");
                admin.setRole("admin");
                admin.setStatus("enabled");
                admin.setCreatedTime(LocalDateTime.now());
                admin.setUpdatedTime(LocalDateTime.now());
                
                userRepository.save(admin);
                log.info("默认管理员用户创建成功: admin/123456");
            }
        }
    }
}