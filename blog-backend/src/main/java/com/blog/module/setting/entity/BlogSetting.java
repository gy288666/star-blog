package com.blog.module.setting.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog_setting")
public class BlogSetting {

    /** 主键即设置键名 */
    @TableId
    private String settingKey;

    private String settingValue;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
