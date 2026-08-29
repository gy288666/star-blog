package com.blog.module.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 游客发表评论请求体（管理员回复复用，免验证码）。 */
@Data
public class CommentCreateDTO {

    @NotNull(message = "文章不能为空")
    private Long postId;

    private Long parentId;

    private Long rootId;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 100, message = "昵称过长")
    private String author;

    @Size(max = 100, message = "邮箱过长")
    private String email;

    @Size(max = 500, message = "网址过长")
    private String website;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 5000, message = "评论内容过长")
    private String content;

    /** 游客必填；管理员回复忽略 */
    private String captchaKey;

    private String captchaCode;
}
