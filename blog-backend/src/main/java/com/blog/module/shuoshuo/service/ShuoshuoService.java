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
import com.blog.util.IpUtils;
import com.blog.util.KVCache;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;

/**
 * 说说：blog_post(type=2) + 点赞去重表。
 * 任意访客可发布（guest_key 标识发布人），仅管理员或发布人本人可删除。
 */
@Service
@RequiredArgsConstructor
public class ShuoshuoService {

    private final PostMapper postMapper;
    private final PostService postService;
    private final ShuoshuoLikeMapper likeMapper;
    private final KVCache kvCache;

    /** 游客发布限流：同一 IP 60 秒一条 */
    private static final long GUEST_POST_COOLDOWN = 60_000;

    public IPage<PostVO> page(long page, long size, String guestId) {
        IPage<BlogPost> result = postMapper.selectPage(Page.of(page, Math.min(size, 50)),
                new LambdaQueryWrapper<BlogPost>()
                        .eq(BlogPost::getType, Constants.TYPE_SHUOSHUO)
                        .eq(BlogPost::getStatus, Constants.STATUS_PUBLISHED)
                        .orderByDesc(BlogPost::getPublishedAt).orderByDesc(BlogPost::getId));
        result.getRecords().forEach(p -> p.setPassword(null));
        IPage<PostVO> voPage = postService.fillRelations(result.convert(PostVO::from), false);
        // 游客说说的昵称映射到 authorNickname（无系统账号）
        voPage.getRecords().forEach(v -> {
            if (v.getAuthorNickname() == null && v.getGuestName() != null) {
                v.setAuthorNickname(v.getGuestName());
            }
        });
        // 标记"我的说说"：guestId 与记录的 guest_key 一致
        if (StringUtils.hasText(guestId)) {
            voPage.getRecords().forEach(v -> v.setMine(v.getId() != null && guestId.equals(v.getGuestKey())));
        }
        // guestKey 是删除凭证，绝不外泄（mine 布尔已足够前端展示）
        voPage.getRecords().forEach(v -> v.setGuestKey(null));
        return voPage;
    }

    /** 游客发布说说：直接过审，同 IP 限流。返回带 guestKey 供前端核对。 */
    public PostVO publishGuest(String content, String nickname, String guestId, String ip) {
        String throttleKey = "shuoshuo:post:" + IpUtils.sha256(ip);
        if (kvCache.has(throttleKey)) {
            throw new BizException("发得太快啦，休息一下再发");
        }
        kvCache.put(throttleKey, 1, GUEST_POST_COOLDOWN);
        if (!StringUtils.hasText(content) || content.trim().length() < 2) {
            throw new BizException("说说至少写 2 个字");
        }
        if (content.length() > 1000) {
            throw new BizException("说说最长 1000 字");
        }
        String key = StringUtils.hasText(guestId) ? guestId.trim() : UUID.randomUUID().toString().replace("-", "");
        BlogPost post = new BlogPost();
        post.setType(Constants.TYPE_SHUOSHUO);
        post.setTitle("");
        post.setContentMd(content.trim());
        post.setContentHtml("");
        post.setStatus(Constants.STATUS_PUBLISHED);
        post.setAllowComment(1);
        post.setViews(0);
        post.setUpvotes(0);
        post.setCommentCount(0);
        post.setGuestKey(key);
        post.setGuestName(StringUtils.hasText(nickname) ? nickname.trim().substring(0, Math.min(50, nickname.trim().length())) : "匿名访客");
        post.setPublishedAt(java.time.LocalDateTime.now());
        postMapper.insert(post);
        PostVO vo = PostVO.from(post);
        vo.setAuthorNickname(post.getGuestName());
        vo.setMine(true);
        vo.setGuestKey(null);
        return vo;
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
