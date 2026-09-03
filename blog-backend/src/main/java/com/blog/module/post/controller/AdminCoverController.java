package com.blog.module.post.controller;

import com.blog.common.result.Result;
import com.blog.module.post.service.CoverRefreshService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 管理端封面刷新（手动触发每周任务）。 */
@RestController
@RequestMapping("/api/admin/covers")
@RequiredArgsConstructor
public class AdminCoverController {

    private final CoverRefreshService coverRefreshService;

    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh() {
        int n = coverRefreshService.refresh();
        return Result.ok(Map.of("updated", n));
    }
}
