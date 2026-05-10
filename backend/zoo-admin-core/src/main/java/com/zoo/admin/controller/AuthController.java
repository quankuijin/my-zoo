package com.zoo.admin.controller;

import com.zoo.admin.service.AuthService;
import com.zoo.admin.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public ResponseEntity<Map<String, Object>> getCaptcha() {
        return ResponseEntity.ok(captchaService.generateCaptcha());
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> request) {
        String sessionId = (String) request.get("sessionId");
        Integer selectedIndex = (Integer) request.get("selectedIndex");
        String username = (String) request.get("username");
        String password = (String) request.get("password");

        if (username == null || password == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "用户名和密码不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        if (sessionId == null || selectedIndex == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "请完成验证码验证");
            return ResponseEntity.badRequest().body(error);
        }

        boolean captchaValid = captchaService.validateCaptcha(sessionId, selectedIndex);
        if (!captchaValid) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "验证码无效或已过期");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            Map<String, Object> result = authService.login(username, password);
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "登出成功");
        return ResponseEntity.ok(result);
    }
}
