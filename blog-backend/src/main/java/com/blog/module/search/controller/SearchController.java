package com.blog.module.search.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.common.constant.Constants;
import com.blog.common.result.PageVO;
import com.blog.common.result.Result;
import com.blog.module.post.service.PostService;
import com.blog.module.post.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/** 公开搜索：?q=&type=article,page,shuoshuo。 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final PostService postService;

    @GetMapping
    public Result<PageVO<PostVO>> search(@RequestParam String q,
                                         @RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(defaultValue = "article") String type) {
        List<Integer> types = new ArrayList<>();
        for (String t : type.split(",")) {
            switch (t.trim()) {
                case "article" -> types.add(Constants.TYPE_ARTICLE);
                case "page" -> types.add(Constants.TYPE_PAGE);
                case "shuoshuo" -> types.add(Constants.TYPE_SHUOSHUO);
            }
        }
        if (types.isEmpty()) {
            types.add(Constants.TYPE_ARTICLE);
        }
        IPage<PostVO> result = postService.search(q, page, size, types);
        result.getRecords().forEach(PostVO::stripContent);
        return Result.ok(PageVO.of(result));
    }
}
