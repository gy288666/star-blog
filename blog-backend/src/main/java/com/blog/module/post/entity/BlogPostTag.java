package com.blog.module.post.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 文章-标签关联。 */
@Data
@TableName("blog_post_tag")
public class BlogPostTag {

    private Long postId;

    private Long tagId;
}
