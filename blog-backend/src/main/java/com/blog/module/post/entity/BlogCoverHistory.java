package com.blog.module.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 已使用过的封面文件 id（保证随机封面全局不重复）。 */
@Data
@TableName("blog_cover_history")
public class BlogCoverHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileId;

    private LocalDateTime usedAt;
}
