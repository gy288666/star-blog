package com.blog.module.comment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.common.result.PageVO;
import com.blog.common.result.Result;
import com.blog.module.comment.dto.CommentCreateDTO;
import com.blog.module.comment.service.CommentService;
import com.blog.module.comment.vo.CommentVO;
import com.blog.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 公开评论接口。 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public Result<PageVO<CommentVO>> page(@RequestParam Long postId,
                                          @RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size) {
        IPage<CommentVO> result = commentService.pageByPost(postId, page, size);
        return Result.ok(new PageVO<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @PostMapping
    public Result<CommentVO> create(@Valid @RequestBody CommentCreateDTO dto, HttpServletRequest request) {
        return Result.ok(commentService.create(dto, IpUtils.getClientIp(request), request.getHeader("User-Agent")));
    }
}
