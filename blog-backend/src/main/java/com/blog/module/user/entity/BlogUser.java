package com.blog.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog_user")
public class BlogUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String nickname;

    private String password;

    private String email;

    private String avatar;

    /** admin / editor */
    private String role;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
