package com.blog.module.tag.controller;

import com.blog.common.result.Result;
import com.blog.module.tag.entity.BlogTag;
import com.blog.module.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 管理端标签 CRUD。 */
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<TagService.TagVO>> list() {
        return Result.ok(tagService.listWithCount());
    }

    @PostMapping
    public Result<BlogTag> save(@RequestBody BlogTag tag) {
        return Result.ok(tagService.save(tag));
    }

    @PutMapping("/{id}")
    public Result<BlogTag> update(@PathVariable Long id, @RequestBody BlogTag tag) {
        tag.setId(id);
        return Result.ok(tagService.save(tag));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.ok();
    }
}
