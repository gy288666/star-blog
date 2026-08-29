package com.blog.module.friend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog_friend")
public class BlogFriend {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String url;

    private String avatar;

    private String description;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;
}
