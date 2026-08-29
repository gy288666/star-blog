package com.blog.module.banner.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.result.Result;
import com.blog.module.banner.entity.BlogBanner;
import com.blog.module.banner.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 管理端横幅 CRUD。 */
@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerMapper bannerMapper;

    @GetMapping
    public Result<List<BlogBanner>> list() {
        return Result.ok(bannerMapper.selectList(new LambdaQueryWrapper<BlogBanner>().orderByDesc(BlogBanner::getId)));
    }

    @PostMapping
    public Result<BlogBanner> save(@RequestBody BlogBanner banner) {
        if (banner.getId() == null) {
            bannerMapper.insert(banner);
        } else {
            bannerMapper.updateById(banner);
        }
        return Result.ok(banner);
    }

    @PutMapping("/{id}")
    public Result<BlogBanner> update(@PathVariable Long id, @RequestBody BlogBanner banner) {
        banner.setId(id);
        bannerMapper.updateById(banner);
        return Result.ok(banner);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return Result.ok();
    }
}
