package com.blog.module.setting.controller;

import com.blog.common.result.Result;
import com.blog.module.setting.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 管理端设置读写（全量键值）。 */
@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class AdminSettingController {

    private final SettingService settingService;

    @GetMapping
    public Result<Map<String, String>> all() {
        return Result.ok(settingService.all());
    }

    @PutMapping
    public Result<Void> save(@RequestBody Map<String, String> settings) {
        settingService.save(settings);
        return Result.ok();
    }
}
