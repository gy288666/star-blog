package com.blog.module.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.module.category.entity.BlogCategory;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface CategoryMapper extends BaseMapper<BlogCategory> {

    /** 各分类下已发布文章数。 */
    @Select("SELECT pc.category_id AS cid, COUNT(*) AS cnt FROM blog_post_category pc " +
            "JOIN blog_post p ON p.id = pc.post_id WHERE p.status = 1 AND p.type = 0 GROUP BY pc.category_id")
    List<Map<String, Object>> countPublishedPosts();
}
