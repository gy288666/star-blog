package com.blog.module.shuoshuo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.common.result.PageVO;
import com.blog.common.result.Result;
import com.blog.module.comment.service.CommentService;
import com.blog.module.comment.vo.CommentVO;
import com.blog.module.shuoshuo.service.ShuoshuoService;
import com.blog.module.post.vo.PostVO;
import com.blog.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 公开说说接口。 */
@RestController
@RequestMapping("/api/shuoshuos")
@RequiredArgsConstructor
public class ShuoshuoController {

    private final ShuoshuoService shuoshuoService;
    private final CommentService commentService;

    @GetMapping
    public Result<PageVO<PostVO>> page(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size) {
        IPage<PostVO> result = shuoshuoService.page(page, size);
        return Result.ok(PageVO.of(result));
    }

    @PostMapping("/{id}/like")
    public Result<java.util.Map<String, Object>> like(@PathVariable Long id, HttpServletRequest request) {
        String unionKey = IpUtils.sha256(IpUtils.getClientIp(request) + request.getHeader("User-Agent"));
        return Result.ok(shuoshuoService.like(id, unionKey));
    }

    /** 说说评论（与文章评论同一张表）。 */
    @GetMapping("/{id}/comments")
    public Result<PageVO<CommentVO>> comments(@PathVariable Long id,
                                              @RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size) {
        IPage<CommentVO> result = commentService.pageByPost(id, page, size);
        return Result.ok(new PageVO<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }
}
