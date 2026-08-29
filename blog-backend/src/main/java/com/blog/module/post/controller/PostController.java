package com.blog.module.post.controller;

import com.blog.common.result.PageVO;
import com.blog.common.result.Result;
import com.blog.module.post.service.PostService;
import com.blog.module.post.vo.PostVO;
import com.blog.util.IpUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 公开文章接口。 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public Result<PageVO<PostVO>> page(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) Long categoryId,
                                       @RequestParam(required = false) Long tagId) {
        IPage<PostVO> result = postService.pageArticles(page, size, categoryId, tagId);
        result.getRecords().forEach(PostVO::stripContent);
        return Result.ok(PageVO.of(result));
    }

    /** id 或 slug 均可；密码文章传 ?password=。 */
    @GetMapping("/{idOrSlug}")
    public Result<Map<String, Object>> detail(@PathVariable String idOrSlug,
                                              @RequestParam(required = false) String password) {
        return Result.ok(postService.detail(idOrSlug, password));
    }

    @GetMapping("/{id}/related")
    public Result<List<PostVO>> related(@PathVariable Long id) {
        return Result.ok(postService.related(id));
    }

    @PutMapping("/{id}/views")
    public Result<Map<String, Integer>> views(@PathVariable Long id, HttpServletRequest request) {
        String dedup = IpUtils.sha256(IpUtils.getClientIp(request) + request.getHeader("User-Agent"));
        return Result.ok(postService.incrViews(id, dedup));
    }
}
