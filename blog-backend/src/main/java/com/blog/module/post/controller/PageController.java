package com.blog.module.post.controller;

import com.blog.common.result.Result;
import com.blog.module.post.service.PostService;
import com.blog.module.post.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 公开单页接口（type=1：关于/留言板等）。 */
@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class PageController {

    private final PostService postService;

    @GetMapping("/{slug}")
    public Result<PostVO> detail(@PathVariable String slug) {
        return Result.ok(postService.pageBySlug(slug));
    }
}
