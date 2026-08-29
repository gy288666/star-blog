package com.blog.module.post.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.common.result.PageVO;
import com.blog.common.result.Result;
import com.blog.module.post.dto.PostSaveDTO;
import com.blog.module.post.service.PostService;
import com.blog.module.post.vo.PostVO;
import com.blog.security.LoginPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** 管理端文章/页面/说说 CRUD。 */
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final PostService postService;

    @GetMapping
    public Result<PageVO<PostVO>> page(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) Integer type,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) String keyword) {
        IPage<PostVO> result = postService.adminPage(page, size, type, status, keyword);
        return Result.ok(PageVO.of(result));
    }

    @GetMapping("/{id}")
    public Result<PostVO> detail(@PathVariable Long id) {
        return Result.ok(postService.adminDetail(id));
    }

    @PostMapping
    public Result<PostVO> create(@Valid @RequestBody PostSaveDTO dto,
                                 @AuthenticationPrincipal LoginPrincipal user) {
        return Result.ok(postService.create(dto, user));
    }

    @PutMapping("/{id}")
    public Result<PostVO> update(@PathVariable Long id, @Valid @RequestBody PostSaveDTO dto) {
        return Result.ok(postService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return Result.ok();
    }
}
