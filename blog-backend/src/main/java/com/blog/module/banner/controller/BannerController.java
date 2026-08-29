package com.blog.module.banner.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.result.Result;
import com.blog.module.banner.entity.BlogBanner;
import com.blog.module.banner.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 公开活跃横幅。 */
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerMapper bannerMapper;

    @GetMapping
    public Result<List<BlogBanner>> list() {
        return Result.ok(bannerMapper.selectList(new LambdaQueryWrapper<BlogBanner>()
                .eq(BlogBanner::getIsActive, 1)
                .orderByDesc(BlogBanner::getId)));
    }
}
