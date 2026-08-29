package com.blog.common.config;

import com.blog.module.user.entity.BlogUser;
import com.blog.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 种子数据：启动时 blog_user 为空则创建管理员 admin / admin123（BCrypt，昵称"站长"）。
 * 不使用 data.sql，避免 Flyway 校验问题。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeedDataRunner implements ApplicationRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userMapper.selectCount(null) > 0) {
            return;
        }
        BlogUser admin = new BlogUser();
        admin.setUsername("admin");
        admin.setNickname("站长");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole("admin");
        admin.setStatus(1);
        userMapper.insert(admin);
        log.info("已初始化管理员账号 admin / admin123，请尽快修改密码");
    }
}
