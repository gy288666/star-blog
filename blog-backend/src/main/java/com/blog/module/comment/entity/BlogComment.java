package com.blog.module.comment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog_comment")
public class BlogComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long parentId;

    /** 根评论为 0，子评论为根评论 id */
    private Long rootId;

    private String author;

    private String email;

    private String website;

    private String avatar;

    private String contentMd;

    private String contentHtml;

    private String ip;

    private String userAgent;

    /** 0=pending, 1=approved, 2=spam */
    private Integer status;

    private Integer isAdmin;

    private LocalDateTime createTime;
}
