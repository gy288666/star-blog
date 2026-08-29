package com.blog.module.comment.vo;

import com.blog.module.comment.entity.BlogComment;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** 公开评论 VO：根评论分页，children 内嵌二级。 */
@Data
public class CommentVO {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long id;
    private Long postId;
    private Long parentId;
    private Long rootId;
    private String author;
    private String website;
    private String avatar;
    private String contentMd;
    private String contentHtml;
    private Integer isAdmin;
    private String createTime;
    private List<CommentVO> children = new ArrayList<>();

    public static CommentVO from(BlogComment c) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setPostId(c.getPostId());
        vo.setParentId(c.getParentId());
        vo.setRootId(c.getRootId());
        vo.setAuthor(c.getAuthor());
        vo.setWebsite(c.getWebsite());
        vo.setAvatar(c.getAvatar());
        vo.setContentMd(c.getContentMd());
        vo.setContentHtml(c.getContentHtml());
        vo.setIsAdmin(c.getIsAdmin());
        vo.setCreateTime(c.getCreateTime() == null ? null : c.getCreateTime().format(DT));
        return vo;
    }
}
