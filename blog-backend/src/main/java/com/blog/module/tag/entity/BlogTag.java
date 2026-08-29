package com.blog.module.tag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog_tag")
public class BlogTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String slug;

    private LocalDateTime createTime;
}
