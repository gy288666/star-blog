package com.blog.module.banner.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog_banner")
public class BlogBanner {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String subtitle;

    private String imageUrl;

    private String bgColor;

    private Integer typingEffect;

    private Integer isActive;

    private LocalDateTime createTime;
}
