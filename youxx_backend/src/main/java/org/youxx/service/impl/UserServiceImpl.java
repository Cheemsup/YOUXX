package org.youxx.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.youxx.common.result.PageResult;
import org.youxx.common.security.PasswordEncoder;
import org.youxx.entity.User;
import org.youxx.entity.UserAddress;
import org.youxx.mapper.UserAddressMapper;
import org.youxx.mapper.UserMapper;
import org.youxx.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserAddressMapper addressMapper;

    @Override
    public PageResult<User> listUsers(String keyword, String role, int page, int size) {
        long total = userMapper.count(keyword, role);
        int offset = (page - 1) * size;
        List<User> records = userMapper.selectPage(keyword, role, offset, size);
        return PageResult.of(records, total, page, size);
    }

    @Override
    public User getUser(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
        return user;
    }

    @Override
    public User addUser(User user) {
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在: " + user.getUsername());
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        user.setPassword(PasswordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus("NORMAL");
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }
        if (user.getUpdateTime() == null) {
            user.setUpdateTime(LocalDateTime.now());
        }
        userMapper.insert(user);
        log.info("用户已创建: id={}, username={}", user.getId(), user.getUsername());
        return userMapper.selectById(user.getId());
    }

    @Override
    public User updateUser(String id, User user) {
        getUser(id);
        user.setId(id);
        userMapper.update(user);
        log.info("用户已更新: id={}", id);
        return userMapper.selectById(id);
    }

    @Override
    public void updateStatus(String id, String status) {
        getUser(id);
        userMapper.updateStatus(id, status);
        log.info("用户状态已更新: id={}, status={}", id, status);
    }

    @Override
    public void deleteUser(String id) {
        getUser(id);
        userMapper.deleteById(id);
        log.info("用户已删除: id={}", id);
    }

    @Override
    public User getProfile(String id) {
        return getUser(id);
    }

    @Override
    public User updateProfile(String id, User user) {
        getUser(id);
        user.setId(id);
        userMapper.updateProfile(user);
        log.info("个人信息已更新: id={}", id);
        return userMapper.selectById(id);
    }

    @Override
    public void updatePassword(String id, String oldPassword, String newPassword) {
        User user = userMapper.selectByIdWithPassword(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!PasswordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("新密码不能为空");
        }
        userMapper.updatePassword(id, PasswordEncoder.encode(newPassword));
        log.info("密码已修改: id={}", id);
    }

    // ==================== 地址管理 ====================

    @Override
    public List<UserAddress> listAddresses(String userId) {
        return addressMapper.selectByUserId(userId);
    }

    @Override
    public UserAddress addAddress(UserAddress address) {
        if (address.getIsDefault() != null && address.getIsDefault()) {
            addressMapper.clearDefaultByUserId(address.getUserId());
        }
        if (address.getCreateTime() == null) {
            address.setCreateTime(LocalDateTime.now());
        }
        addressMapper.insert(address);
        log.info("地址已添加: userId={}, id={}", address.getUserId(), address.getId());
        return address;
    }

    @Override
    public UserAddress updateAddress(Long id, UserAddress address) {
        address.setId(id);
        addressMapper.update(address);
        log.info("地址已更新: id={}", id);
        return addressMapper.selectById(id);
    }

    @Override
    public void deleteAddress(Long id) {
        addressMapper.deleteById(id);
        log.info("地址已删除: id={}", id);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long id) {
        UserAddress address = addressMapper.selectById(id);
        if (address == null) {
            throw new IllegalArgumentException("地址不存在: " + id);
        }
        addressMapper.clearDefaultByUserId(address.getUserId());
        addressMapper.setDefault(id);
        log.info("默认地址已设置: id={}, userId={}", id, address.getUserId());
    }
}