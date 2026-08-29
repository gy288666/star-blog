package com.blog.module.setting.controller;

import com.blog.common.result.Result;
import com.blog.module.setting.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 公开设置（白名单键）。 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @GetMapping("/public")
    public Result<Map<String, String>> publicSettings() {
        return Result.ok(settingService.publicMap());
    }
}
