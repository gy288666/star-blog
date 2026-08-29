package com.blog.module.category.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.exception.BizException;
import com.blog.module.category.entity.BlogCategory;
import com.blog.module.category.mapper.CategoryMapper;
import com.blog.module.post.entity.BlogPostCategory;
import com.blog.module.post.mapper.PostCategoryMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final PostCategoryMapper postCategoryMapper;

    /** VO：带文章数。 */
    @Data
    public static class CategoryVO {
        private Long id;
        private String name;
        private String slug;
        private String description;
        private Long parentId;
        private Integer sortOrder;
        private Long postCount;
    }

    public List<CategoryVO> listWithCount() {
        List<BlogCategory> all = categoryMapper.selectList(
                new LambdaQueryWrapper<BlogCategory>().orderByAsc(BlogCategory::getSortOrder).orderByAsc(BlogCategory::getId));
        Map<Long, Long> counts = categoryMapper.countPublishedPosts().stream()
                .collect(Collectors.toMap(m -> ((Number) m.get("cid")).longValue(),
                        m -> ((Number) m.get("cnt")).longValue()));
        return all.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setSlug(c.getSlug());
            vo.setDescription(c.getDescription());
            vo.setParentId(c.getParentId());
            vo.setSortOrder(c.getSortOrder());
            vo.setPostCount(counts.getOrDefault(c.getId(), 0L));
            return vo;
        }).collect(Collectors.toList());
    }

    public BlogCategory bySlug(String slug) {
        BlogCategory c = categoryMapper.selectOne(new LambdaQueryWrapper<BlogCategory>().eq(BlogCategory::getSlug, slug).last("LIMIT 1"));
        if (c == null) {
            throw new BizException(404, "分类不存在");
        }
        return c;
    }

    @Transactional
    public BlogCategory save(BlogCategory category) {
        if (!StringUtils.hasText(category.getName())) {
            throw new BizException("分类名不能为空");
        }
        if (category.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
        return category;
    }

    @Transactional
    public void delete(Long id) {
        postCategoryMapper.delete(new LambdaQueryWrapper<BlogPostCategory>().eq(BlogPostCategory::getCategoryId, id));
        categoryMapper.deleteById(id);
    }
}
