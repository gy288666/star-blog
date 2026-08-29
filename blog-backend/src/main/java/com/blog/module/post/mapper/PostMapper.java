package com.blog.module.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.module.post.entity.BlogPost;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface PostMapper extends BaseMapper<BlogPost> {

    @Select("SELECT p.* FROM blog_post p JOIN blog_post_category pc ON pc.post_id = p.id " +
            "WHERE pc.category_id = #{categoryId} AND p.type = 0 AND p.status = 1")
    IPage<BlogPost> selectByCategory(Page<?> page, @Param("categoryId") Long categoryId);

    @Select("SELECT p.* FROM blog_post p JOIN blog_post_tag pt ON pt.post_id = p.id " +
            "WHERE pt.tag_id = #{tagId} AND p.type = 0 AND p.status = 1")
    IPage<BlogPost> selectByTag(Page<?> page, @Param("tagId") Long tagId);

    /** FULLTEXT ngram 布尔模式搜索，失败时上层回退 LIKE。 */
    @Select("<script>SELECT * FROM blog_post WHERE status = 1 AND type IN " +
            "<foreach item='t' collection='types' open='(' separator=',' close=')'>#{t}</foreach> " +
            "AND MATCH(title, content_md) AGAINST(#{q} IN BOOLEAN MODE)</script>")
    IPage<BlogPost> selectFulltext(Page<?> page, @Param("q") String q, @Param("types") List<Integer> types);

    /** 相关文章：同分类或同标签，最多 6 篇。 */
    @Select("SELECT DISTINCT p.* FROM blog_post p WHERE p.id <> #{id} AND p.type = 0 AND p.status = 1 AND (" +
            "p.id IN (SELECT post_id FROM blog_post_category WHERE category_id IN " +
            "(SELECT category_id FROM blog_post_category WHERE post_id = #{id})) OR " +
            "p.id IN (SELECT post_id FROM blog_post_tag WHERE tag_id IN " +
            "(SELECT tag_id FROM blog_post_tag WHERE post_id = #{id}))) " +
            "ORDER BY p.views DESC LIMIT 6")
    List<BlogPost> selectRelated(@Param("id") Long id);

    @Select("SELECT * FROM blog_post WHERE type = 0 AND status = 1 AND published_at IS NOT NULL " +
            "AND published_at < #{publishedAt} ORDER BY published_at DESC LIMIT 1")
    BlogPost selectPrev(@Param("publishedAt") LocalDateTime publishedAt);

    @Select("SELECT * FROM blog_post WHERE type = 0 AND status = 1 AND published_at IS NOT NULL " +
            "AND published_at > #{publishedAt} ORDER BY published_at ASC LIMIT 1")
    BlogPost selectNext(@Param("publishedAt") LocalDateTime publishedAt);
}
