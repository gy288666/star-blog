package com.blog.module.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.module.comment.entity.BlogComment;
import com.blog.module.comment.vo.CommentAdminVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

public interface CommentMapper extends BaseMapper<BlogComment> {

    /** 批量取根评论的已审核子评论。 */
    @Select("<script>SELECT * FROM blog_comment WHERE status = 1 AND root_id IN " +
            "<foreach item='r' collection='rootIds' open='(' separator=',' close=')'>#{r}</foreach> " +
            "ORDER BY create_time ASC, id ASC</script>")
    List<BlogComment> selectChildrenByRoots(@Param("rootIds") Collection<Long> rootIds);

    /** 管理端扁平列表，带文章标题。 */
    @Select("<script>SELECT c.*, p.title AS post_title FROM blog_comment c " +
            "LEFT JOIN blog_post p ON p.id = c.post_id " +
            "<where><if test='status != null'>c.status = #{status}</if>" +
            "<if test='postId != null'>AND c.post_id = #{postId}</if></where> " +
            "ORDER BY c.create_time DESC, c.id DESC</script>")
    IPage<CommentAdminVO> selectAdminPage(Page<?> page, @Param("status") Integer status, @Param("postId") Long postId);
}
