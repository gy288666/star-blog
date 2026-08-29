package com.blog.module.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog_post")
public class BlogPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 0=article, 1=page, 2=shuoshuo */
    private Integer type;

    private String title;

    private String slug;

    private String contentMd;

    private String contentHtml;

    private String summary;

    private String cover;

    /** 0=draft, 1=publish, 2=password */
    private Integer status;

    private String password;

    private Long authorId;

    private Integer views;

    private Integer upvotes;

    private Integer isTop;

    private Integer allowComment;

    private Integer commentCount;

    private LocalDateTime publishedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
