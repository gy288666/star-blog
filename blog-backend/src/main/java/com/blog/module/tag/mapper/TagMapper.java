package com.blog.module.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.module.tag.entity.BlogTag;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface TagMapper extends BaseMapper<BlogTag> {

    /** 各标签下已发布文章数。 */
    @Select("SELECT pt.tag_id AS tid, COUNT(*) AS cnt FROM blog_post_tag pt " +
            "JOIN blog_post p ON p.id = pt.post_id WHERE p.status = 1 AND p.type = 0 GROUP BY pt.tag_id")
    List<Map<String, Object>> countPublishedPosts();
}
