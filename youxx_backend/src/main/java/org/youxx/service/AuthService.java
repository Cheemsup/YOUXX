package org.youxx.service;

import org.youxx.entity.User;

import java.util.Map;

public interface AuthService {

    Map<String, Object> login(String username, String password, String role);

    User register(String username, String password, String phone);

    User getInfo(String userId);
}