package com.blog.module.post.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 文章-分类关联。 */
@Data
@TableName("blog_post_category")
public class BlogPostCategory {

    private Long postId;

    private Long categoryId;
}
