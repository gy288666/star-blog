package com.blog.module.stat.controller;

import com.blog.common.result.Result;
import com.blog.module.stat.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 管理端统计。 */
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatController {

    private final StatService statService;

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        return Result.ok(statService.summary());
    }

    @GetMapping("/views")
    public Result<List<Map<String, Object>>> views(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(statService.viewsTrend(days));
    }

    @GetMapping("/topPosts")
    public Result<List<Map<String, Object>>> topPosts(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(statService.topPosts(limit));
    }
}
