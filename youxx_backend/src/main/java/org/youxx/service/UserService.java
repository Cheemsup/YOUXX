package org.youxx.service;

import org.youxx.common.result.PageResult;
import org.youxx.entity.User;
import org.youxx.entity.UserAddress;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    PageResult<User> listUsers(String keyword, String role, int page, int size);

    User getUser(String id);

    User addUser(User user);

    User updateUser(String id, User user);

    void updateStatus(String id, String status);

    void deleteUser(String id);

    User getProfile(String id);

    User updateProfile(String id, User user);

    /** 上传用户头像：落盘到 upload_resources/user_icon/，返回可访问的资源路径 */
    String uploadAvatar(MultipartFile file);

    void updatePassword(String id, String oldPassword, String newPassword);

    List<UserAddress> listAddresses(String userId);

    UserAddress addAddress(UserAddress address);

    UserAddress updateAddress(Long id, UserAddress address);

    void deleteAddress(Long id);

    void setDefaultAddress(Long id);
}