package com.blog.module.shuoshuo.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** 游客发布说说请求体。 */
@Data
public class ShuoshuoPublishDTO {

    @Size(min = 2, max = 1000, message = "说说长度需在 2-1000 字之间")
    private String content;

    /** 游客昵称，可空（默认"匿名访客"） */
    @Size(max = 50, message = "昵称过长")
    private String nickname;
}
