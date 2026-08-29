package com.blog.module.shuoshuo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog_shuoshuo_like")
public class BlogShuoshuoLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    /** SHA256(ip+ua) */
    private String unionKey;

    private LocalDateTime createTime;
}
