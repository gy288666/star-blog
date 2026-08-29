package com.blog.module.comment.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 管理端扁平评论行（Mapper 联表 post_title）。 */
@Data
public class CommentAdminVO {

    private Long id;
    private Long postId;
    private Long parentId;
    private Long rootId;
    private String author;
    private String email;
    private String website;
    private String contentMd;
    private String contentHtml;
    private String ip;
    private String userAgent;
    private Integer status;
    private Integer isAdmin;
    private LocalDateTime createTime;
    /** blog_post.title */
    private String postTitle;
}
