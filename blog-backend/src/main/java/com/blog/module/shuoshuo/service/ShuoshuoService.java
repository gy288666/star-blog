package com.blog.module.shuoshuo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.constant.Constants;
import com.blog.common.exception.BizException;
import com.blog.module.post.entity.BlogPost;
import com.blog.module.post.mapper.PostMapper;
import com.blog.module.post.service.PostService;
import com.blog.module.post.vo.PostVO;
import com.blog.module.shuoshuo.entity.BlogShuoshuoLike;
import com.blog.module.shuoshuo.mapper.ShuoshuoLikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Map;

/** 说说：blog_post(type=2) + 点赞去重表。 */
@Service
@RequiredArgsConstructor
public class ShuoshuoService {

    private final PostMapper postMapper;
    private final PostService postService;
    private final ShuoshuoLikeMapper likeMapper;

    public IPage<PostVO> page(long page, long size) {
        IPage<BlogPost> result = postMapper.selectPage(Page.of(page, Math.min(size, 50)),
                new LambdaQueryWrapper<BlogPost>()
                        .eq(BlogPost::getType, Constants.TYPE_SHUOSHUO)
                        .eq(BlogPost::getStatus, Constants.STATUS_PUBLISHED)
                        .orderByDesc(BlogPost::getPublishedAt).orderByDesc(BlogPost::getId));
        result.getRecords().forEach(p -> p.setPassword(null));
        return postService.fillRelations(result.convert(PostVO::from), false);
    }

    /** 点赞：唯一键冲突说明已赞过，不重复计数。 */
    public Map<String, Object> like(Long id, String unionKey) {
        BlogPost post = postMapper.selectById(id);
        if (post == null || post.getType() != Constants.TYPE_SHUOSHUO) {
            throw new BizException(404, "说说不存在");
        }
        boolean inserted = false;
        try {
            BlogShuoshuoLike like = new BlogShuoshuoLike();
            like.setPostId(id);
            like.setUnionKey(unionKey);
            likeMapper.insert(like);
            inserted = true;
        } catch (DuplicateKeyException ignored) {
            // 已点过赞
        }
        if (inserted) {
            BlogPost upd = new BlogPost();
            upd.setId(id);
            upd.setUpvotes((post.getUpvotes() == null ? 0 : post.getUpvotes()) + 1);
            postMapper.updateById(upd);
            post.setUpvotes(upd.getUpvotes());
        }
        return Map.of("upvotes", post.getUpvotes() == null ? 0 : post.getUpvotes(), "liked", true);
    }
}
