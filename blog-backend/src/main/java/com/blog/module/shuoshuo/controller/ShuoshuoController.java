package com.blog.module.shuoshuo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.common.exception.BizException;
import com.blog.common.result.PageVO;
import com.blog.common.result.Result;
import com.blog.module.comment.service.CommentService;
import com.blog.module.comment.vo.CommentVO;
import com.blog.module.post.entity.BlogPost;
import com.blog.module.post.mapper.PostMapper;
import com.blog.module.post.vo.PostVO;
import com.blog.module.shuoshuo.dto.ShuoshuoPublishDTO;
import com.blog.module.shuoshuo.service.ShuoshuoService;
import com.blog.security.LoginPrincipal;
import com.blog.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 公开说说接口：游客可发布；删除需管理员身份或发布人 guestId 匹配。
 */
@RestController
@RequestMapping("/api/shuoshuos")
@RequiredArgsConstructor
public class ShuoshuoController {

    private final ShuoshuoService shuoshuoService;
    private final CommentService commentService;
    private final PostMapper postMapper;

    /** guestId 通过 X-Guest-Id 头传递（前端 localStorage 持久化）。 */
    @GetMapping
    public Result<PageVO<PostVO>> page(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestHeader(value = "X-Guest-Id", required = false) String guestId) {
        IPage<PostVO> result = shuoshuoService.page(page, size, guestId);
        return Result.ok(PageVO.of(result));
    }

    /** 游客发布说说。guestId 同样走 X-Guest-Id 头；缺失时由服务端生成并在响应中返回。 */
    @PostMapping
    public Result<PostVO> publish(@Valid @RequestBody ShuoshuoPublishDTO dto,
                                  @RequestHeader(value = "X-Guest-Id", required = false) String guestId,
                                  HttpServletRequest request) {
        PostVO vo = shuoshuoService.publishGuest(dto.getContent(), dto.getNickname(), guestId,
                IpUtils.getClientIp(request));
        return Result.ok(vo);
    }

    /**
     * 删除说说：管理员（Bearer token）或发布人（X-Guest-Id 匹配 guest_key）。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "X-Guest-Id", required = false) String guestId,
                               @AuthenticationPrincipal LoginPrincipal user) {
        BlogPost post = postMapper.selectById(id);
        if (post == null || post.getType() != 2) {
            throw new BizException(404, "说说不存在");
        }
        boolean isAdmin = user != null && "admin".equalsIgnoreCase(user.getRole());
        boolean isOwner = post.getGuestKey() != null && post.getGuestKey().equals(guestId);
        if (!isAdmin && !isOwner) {
            throw new BizException(403, "只能删除自己发布的说说");
        }
        postMapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable Long id, HttpServletRequest request) {
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
