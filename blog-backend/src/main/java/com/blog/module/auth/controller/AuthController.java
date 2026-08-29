package com.blog.module.auth.controller;

import com.blog.common.constant.Constants;
import com.blog.common.exception.BizException;
import com.blog.common.result.Result;
import com.blog.module.user.entity.BlogUser;
import com.blog.module.user.mapper.UserMapper;
import com.blog.security.JwtUtil;
import com.blog.security.LoginPrincipal;
import com.blog.util.CaptchaUtil;
import com.blog.util.KVCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** 登录 + 验证码。 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final KVCache kvCache;

    @Data
    public static class LoginDTO {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @PostMapping("/auth/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginDTO dto) {
        BlogUser user = userMapper.selectOne(new LambdaQueryWrapper<BlogUser>()
                .eq(BlogUser::getUsername, dto.getUsername()).last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(403, "账号已停用");
        }
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("token", token);
        out.put("user", Map.of("id", user.getId(), "username", user.getUsername(),
                "nickname", user.getNickname() == null ? "" : user.getNickname(),
                "avatar", user.getAvatar() == null ? "" : user.getAvatar(),
                "role", user.getRole()));
        return Result.ok(out);
    }

    /** 当前登录用户信息（前端刷新页面时恢复用）。 */
    @GetMapping("/auth/me")
    public Result<Map<String, Object>> me(@AuthenticationPrincipal LoginPrincipal user) {
        return Result.ok(Map.of("id", user.getId(), "username", user.getUsername(),
                "nickname", user.getNickname() == null ? "" : user.getNickname(), "role", user.getRole()));
    }

    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        CaptchaUtil.Captcha c = CaptchaUtil.generate();
        kvCache.put(Constants.CACHE_CAPTCHA + c.key(), c.code(), Constants.TTL_CAPTCHA);
        return Result.ok(Map.of("key", c.key(), "image", c.image()));
    }
}
