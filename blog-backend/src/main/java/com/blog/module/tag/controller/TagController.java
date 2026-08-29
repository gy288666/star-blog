package com.blog.module.tag.controller;

import com.blog.common.result.Result;
import com.blog.module.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 公开标签列表。 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<TagService.TagVO>> list() {
        return Result.ok(tagService.listWithCount());
    }
}
