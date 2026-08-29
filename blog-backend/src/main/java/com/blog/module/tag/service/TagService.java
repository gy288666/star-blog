package com.blog.module.tag.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.exception.BizException;
import com.blog.module.post.entity.BlogPostTag;
import com.blog.module.post.mapper.PostTagMapper;
import com.blog.module.tag.entity.BlogTag;
import com.blog.module.tag.mapper.TagMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;
    private final PostTagMapper postTagMapper;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TagVO extends BlogTag {
        private Long postCount;
    }

    public List<TagVO> listWithCount() {
        List<BlogTag> all = tagMapper.selectList(new LambdaQueryWrapper<BlogTag>().orderByAsc(BlogTag::getId));
        Map<Long, Long> counts = tagMapper.countPublishedPosts().stream()
                .collect(Collectors.toMap(m -> ((Number) m.get("tid")).longValue(),
                        m -> ((Number) m.get("cnt")).longValue()));
        return all.stream().map(t -> {
            TagVO vo = new TagVO();
            vo.setId(t.getId());
            vo.setName(t.getName());
            vo.setSlug(t.getSlug());
            vo.setCreateTime(t.getCreateTime());
            vo.setPostCount(counts.getOrDefault(t.getId(), 0L));
            return vo;
        }).collect(Collectors.toList());
    }

    public BlogTag bySlug(String slug) {
        BlogTag t = tagMapper.selectOne(new LambdaQueryWrapper<BlogTag>().eq(BlogTag::getSlug, slug).last("LIMIT 1"));
        if (t == null) {
            throw new BizException(404, "标签不存在");
        }
        return t;
    }

    @Transactional
    public BlogTag save(BlogTag tag) {
        if (!StringUtils.hasText(tag.getName())) {
            throw new BizException("标签名不能为空");
        }
        if (tag.getId() == null) {
            tagMapper.insert(tag);
        } else {
            tagMapper.updateById(tag);
        }
        return tag;
    }

    @Transactional
    public void delete(Long id) {
        postTagMapper.delete(new LambdaQueryWrapper<BlogPostTag>().eq(BlogPostTag::getTagId, id));
        tagMapper.deleteById(id);
    }
}
