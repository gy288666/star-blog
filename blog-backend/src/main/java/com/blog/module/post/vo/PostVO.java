package com.blog.module.post.vo;

import com.blog.module.post.entity.BlogPost;
import lombok.Data;

import java.util.List;

/** 前端可见的文章 VO；列表场景不填充正文（contentMd/contentHtml 为 null）。 */
@Data
public class PostVO {

    private Long id;
    private Integer type;
    private String title;
    private String slug;
    private String summary;
    private String cover;
    private Integer status;
    private Long authorId;
    private String authorNickname;
    private Integer views;
    private Integer upvotes;
    private Integer isTop;
    private Integer allowComment;
    private Integer commentCount;
    private String publishedAt;
    private String createTime;
    private String updateTime;
    private String contentMd;
    private String contentHtml;
    /** 密码保护的原文密码仅管理端返回 */
    private String password;
    private List<IdName> categories;
    private List<IdName> tags;
    private List<Long> categoryIds;
    private List<Long> tagIds;

    public record IdName(Long id, String name, String slug) {
    }

    public static PostVO from(BlogPost p) {
        PostVO vo = new PostVO();
        vo.setId(p.getId());
        vo.setType(p.getType());
        vo.setTitle(p.getTitle());
        vo.setSlug(p.getSlug());
        vo.setSummary(p.getSummary());
        vo.setCover(p.getCover());
        vo.setStatus(p.getStatus());
        vo.setAuthorId(p.getAuthorId());
        vo.setViews(p.getViews());
        vo.setUpvotes(p.getUpvotes());
        vo.setIsTop(p.getIsTop());
        vo.setAllowComment(p.getAllowComment());
        vo.setCommentCount(p.getCommentCount());
        vo.setPublishedAt(p.getPublishedAt() == null ? null : p.getPublishedAt().toString().replace('T', ' '));
        vo.setCreateTime(p.getCreateTime() == null ? null : p.getCreateTime().toString().replace('T', ' '));
        vo.setUpdateTime(p.getUpdateTime() == null ? null : p.getUpdateTime().toString().replace('T', ' '));
        vo.setContentMd(p.getContentMd());
        vo.setContentHtml(p.getContentHtml());
        vo.setPassword(p.getPassword());
        return vo;
    }

    /** 列表用：去除正文与密码。 */
    public PostVO stripContent() {
        this.contentMd = null;
        this.contentHtml = null;
        this.password = null;
        return this;
    }
}
