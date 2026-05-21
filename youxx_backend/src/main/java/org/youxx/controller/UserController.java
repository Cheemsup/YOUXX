package org.youxx.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.youxx.common.result.PageResult;
import org.youxx.common.result.Result;
import org.youxx.common.userInfoMaintainer.BaseContext;
import org.youxx.entity.User;
import org.youxx.entity.UserAddress;
import org.youxx.service.UserService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ==================== 个人信息（当前登录用户） ====================
    // 注意：这些路径必须在 /{id} 之前，避免被路径变量误匹配

    @GetMapping("/profile")
    public Result<User> profile() {
        User user = userService.getProfile(BaseContext.getCurrentId());
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody User user) {
        User updated = userService.updateProfile(BaseContext.getCurrentId(), user);
        return Result.success(updated);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        userService.updatePassword(BaseContext.getCurrentId(), oldPassword, newPassword);
        return Result.success();
    }

    // ==================== 地址管理 ====================

    @GetMapping("/address")
    public Result<List<UserAddress>> listAddresses() {
        List<UserAddress> addresses = userService.listAddresses(BaseContext.getCurrentId());
        return Result.success(addresses);
    }

    @PostMapping("/address")
    public Result<UserAddress> addAddress(@RequestBody UserAddress address) {
        address.setUserId(BaseContext.getCurrentId());
        UserAddress created = userService.addAddress(address);
        return Result.success(created);
    }

    @PutMapping("/address/{id}")
    public Result<UserAddress> updateAddress(@PathVariable Long id, @RequestBody UserAddress address) {
        UserAddress updated = userService.updateAddress(id, address);
        return Result.success(updated);
    }

    @DeleteMapping("/address/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        userService.deleteAddress(id);
        return Result.success();
    }

    @PutMapping("/address/{id}/default")
    public Result<Void> setDefaultAddress(@PathVariable Long id) {
        userService.setDefaultAddress(id);
        return Result.success();
    }

    // ==================== 用户管理（管理员） ====================

    @GetMapping("/list")
    public Result<PageResult<User>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<User> result = userService.listUsers(keyword, role, page, size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable String id) {
        User user = userService.getUser(id);
        return Result.success(user);
    }

    @PostMapping
    public Result<User> add(@RequestBody User user) {
        User created = userService.addUser(user);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable String id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return Result.success(updated);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        userService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        userService.deleteUser(id);
        return Result.success();
    }
}