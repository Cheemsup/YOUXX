package org.youxx.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.youxx.common.result.Result;
import org.youxx.common.userInfoMaintainer.BaseContext;
import org.youxx.dto.LoginRequest;
import org.youxx.dto.RegisterRequest;
import org.youxx.service.AuthService;
import org.youxx.vo.LoginVO;
import org.youxx.vo.UserVO;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginRequest request) {
        LoginVO result = authService.login(request.getUsername(), request.getPassword());
        return Result.success(result);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "token", required = false) String token) {
        authService.logout(token);
        return Result.success();
    }

    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody RegisterRequest request) {
        UserVO user = authService.register(request.getUsername(), request.getPassword(), request.getPhone());
        return Result.success(user);
    }

    @GetMapping("/info")
    public Result<UserVO> info() {
        String userId = BaseContext.getCurrentId();
        UserVO user = authService.getInfo(userId);
        return Result.success(user);
    }
}
