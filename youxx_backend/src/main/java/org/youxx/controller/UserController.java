package org.youxx.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.youxx.common.result.PageResult;
import org.youxx.common.result.Result;
import org.youxx.common.userInfoMaintainer.BaseContext;
import org.youxx.dto.UpdatePasswordRequest;
import org.youxx.dto.UpdateStatusRequest;
import org.youxx.dto.UserAddressRequest;
import org.youxx.dto.UserUpdateRequest;
import org.youxx.entity.User;
import org.youxx.entity.UserAddress;
import org.youxx.service.UserService;
import org.youxx.vo.UploadVO;
import org.youxx.vo.UserVO;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ==================== 个人信息（当前登录用户） ====================
    // 注意：这些路径必须在 /{id} 之前，避免被路径变量误匹配

    @GetMapping("/profile")
    public Result<UserVO> profile() {
        User user = userService.getProfile(BaseContext.getCurrentId());
        return Result.success(toVO(user));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@RequestBody UserUpdateRequest request) {
        // 普通用户仅可改 phone/email/avatar；密码、角色、状态等不由此接口处理
        User user = new User();
        BeanUtils.copyProperties(request, user);
        User updated = userService.updateProfile(BaseContext.getCurrentId(), user);
        return Result.success(toVO(updated));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(BaseContext.getCurrentId(), request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }

    @PostMapping("/avatar/upload")
    public Result<UploadVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // 头像落盘 + 路径写回当前用户 avatar，一次完成；前端拿到 url 即可直接展示
        String url = userService.uploadAvatar(file);
        User u = new User();
        u.setAvatar(url);
        userService.updateProfile(BaseContext.getCurrentId(), u);
        UploadVO vo = new UploadVO();
        vo.setUrl(url);
        return Result.success(vo);
    }

    // ==================== 地址管理 ====================

    @GetMapping("/address")
    public Result<List<UserAddress>> listAddresses() {
        List<UserAddress> addresses = userService.listAddresses(BaseContext.getCurrentId());
        return Result.success(addresses);
    }

    @PostMapping("/address")
    public Result<UserAddress> addAddress(@RequestBody UserAddressRequest request) {
        UserAddress address = new UserAddress();
        BeanUtils.copyProperties(request, address);
        address.setUserId(BaseContext.getCurrentId());
        UserAddress created = userService.addAddress(address);
        return Result.success(created);
    }

    @PutMapping("/address/{id}")
    public Result<UserAddress> updateAddress(@PathVariable Long id, @RequestBody UserAddressRequest request) {
        UserAddress address = new UserAddress();
        BeanUtils.copyProperties(request, address);
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
    public Result<PageResult<UserVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<User> result = userService.listUsers(keyword, role, page, size);
        List<UserVO> voList = result.getRecords().stream().map(this::toVO).toList();
        return Result.success(PageResult.of(voList, result.getTotal(), page, size));
    }

    @GetMapping("/{id}")
    public Result<UserVO> detail(@PathVariable String id) {
        User user = userService.getUser(id);
        return Result.success(toVO(user));
    }

    @PostMapping
    public Result<UserVO> add(@RequestBody User user) {
        // 管理员新增用户：需设置密码、角色、状态等，保留 entity 入参
        User created = userService.addUser(user);
        return Result.success(toVO(created));
    }

    @PutMapping("/{id}")
    public Result<UserVO> update(@PathVariable String id, @RequestBody User user) {
        // 管理员编辑用户：同上，保留 entity 入参
        User updated = userService.updateUser(id, user);
        return Result.success(toVO(updated));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable String id, @RequestBody UpdateStatusRequest request) {
        userService.updateStatus(id, request.getStatus());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /** User -> UserVO，屏蔽 password */
    private UserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
