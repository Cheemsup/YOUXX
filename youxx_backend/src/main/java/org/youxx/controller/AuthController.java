package org.youxx.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.youxx.common.result.Result;
import org.youxx.common.userInfoMaintainer.BaseContext;
import org.youxx.entity.User;
import org.youxx.service.AuthService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Map<String, Object> result = authService.login(username, password);
        return Result.success(result);
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String phone = body.get("phone");
        User user = authService.register(username, password, phone);
        return Result.success(user);
    }

    @GetMapping("/info")
    public Result<User> info() {
        String userId = BaseContext.getCurrentId();
        User user = authService.getInfo(userId);
        return Result.success(user);
    }
}