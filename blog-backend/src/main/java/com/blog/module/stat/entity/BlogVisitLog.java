package com.blog.module.stat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog_visit_log")
public class BlogVisitLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ip;

    private String url;

    private String referer;

    private String userAgent;

    private LocalDateTime visitTime;
}
