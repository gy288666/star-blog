package com.blog.module.stat.controller;

import com.blog.common.result.Result;
import com.blog.module.stat.service.StatService;
import com.blog.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 前端访问日志上报。 */
@RestController
@RequestMapping("/api/visit")
@RequiredArgsConstructor
public class VisitController {

    private final StatService statService;

    @PostMapping
    public Result<Void> visit(@RequestBody Map<String, String> body, HttpServletRequest request) {
        statService.recordVisit(IpUtils.getClientIp(request),
                body.get("url"), body.get("referer"), request.getHeader("User-Agent"));
        return Result.ok();
    }
}
