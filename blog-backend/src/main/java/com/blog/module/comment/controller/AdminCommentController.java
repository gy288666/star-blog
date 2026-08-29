package com.blog.module.comment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.common.result.PageVO;
import com.blog.common.result.Result;
import com.blog.module.comment.dto.CommentCreateDTO;
import com.blog.module.comment.service.CommentService;
import com.blog.module.comment.vo.CommentAdminVO;
import com.blog.module.comment.vo.CommentVO;
import com.blog.security.LoginPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 管理端评论审核/回复/删除。 */
@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public Result<PageVO<CommentAdminVO>> page(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) Long postId) {
        IPage<CommentAdminVO> result = commentService.adminPage(page, size, status, postId);
        return Result.ok(PageVO.of(result));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || status < 0 || status > 2) {
            return Result.error(400, "status 取值 0/1/2");
        }
        commentService.updateStatus(id, status);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.ok();
    }

    /** 管理员回复（is_admin=1，免验证码）。 */
    @PostMapping
    public Result<CommentVO> reply(@Valid @RequestBody CommentCreateDTO dto,
                                   @AuthenticationPrincipal LoginPrincipal user) {
        return Result.ok(commentService.adminReply(dto, user));
    }
}
