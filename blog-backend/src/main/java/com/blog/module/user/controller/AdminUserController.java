package com.blog.module.user.controller;

import com.blog.common.exception.BizException;
import com.blog.common.result.Result;
import com.blog.module.user.entity.BlogUser;
import com.blog.module.user.mapper.UserMapper;
import com.blog.security.LoginPrincipal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 管理员账号：改密 / 资料。 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminUserController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Data
    public static class PasswordDTO {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 64, message = "新密码长度 6-64 位")
        private String newPassword;
    }

    @PutMapping("/password")
    public Result<Void> password(@Validated @RequestBody PasswordDTO dto,
                                 @AuthenticationPrincipal LoginPrincipal user) {
        BlogUser u = userMapper.selectById(user.getId());
        if (u == null || !passwordEncoder.matches(dto.getOldPassword(), u.getPassword())) {
            throw new BizException("原密码错误");
        }
        BlogUser upd = new BlogUser();
        upd.setId(u.getId());
        upd.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(upd);
        return Result.ok();
    }

    @PutMapping("/profile")
    public Result<Void> profile(@RequestBody Map<String, String> body,
                                @AuthenticationPrincipal LoginPrincipal user) {
        BlogUser upd = new BlogUser();
        upd.setId(user.getId());
        upd.setNickname(body.get("nickname"));
        upd.setAvatar(body.get("avatar"));
        upd.setEmail(body.get("email"));
        userMapper.updateById(upd);
        return Result.ok();
    }
}
