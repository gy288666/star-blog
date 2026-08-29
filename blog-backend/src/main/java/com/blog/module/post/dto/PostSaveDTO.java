package com.blog.module.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 文章/页面/说说创建与更新的请求体。 */
@Data
public class PostSaveDTO {

    /** 0=article, 1=page, 2=shuoshuo */
    @NotNull(message = "type 不能为空")
    private Integer type;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String slug;

    private String summary;

    private String cover;

    private String contentMd;

    /** 0=draft, 1=publish, 2=password */
    private Integer status;

    private String password;

    private Integer isTop;

    private Integer allowComment;

    private List<Long> categoryIds;

    private List<Long> tagIds;

    /** 为空时由后端按状态决定 */
    private String publishedAt;
}
