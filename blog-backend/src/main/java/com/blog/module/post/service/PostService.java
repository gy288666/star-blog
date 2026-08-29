package com.blog.module.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.constant.Constants;
import com.blog.common.exception.BizException;
import com.blog.module.category.entity.BlogCategory;
import com.blog.module.category.mapper.CategoryMapper;
import com.blog.module.post.dto.PostSaveDTO;
import com.blog.module.post.entity.BlogPost;
import com.blog.module.post.entity.BlogPostCategory;
import com.blog.module.post.entity.BlogPostTag;
import com.blog.module.post.mapper.PostCategoryMapper;
import com.blog.module.post.mapper.PostMapper;
import com.blog.module.post.mapper.PostTagMapper;
import com.blog.module.post.vo.PostVO;
import com.blog.module.tag.entity.BlogTag;
import com.blog.module.tag.mapper.TagMapper;
import com.blog.module.user.entity.BlogUser;
import com.blog.module.user.mapper.UserMapper;
import com.blog.security.LoginPrincipal;
import com.blog.util.KVCache;
import com.blog.util.MdConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PostMapper postMapper;
    private final PostCategoryMapper postCategoryMapper;
    private final PostTagMapper postTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final KVCache kvCache;

    // ==================== 公开查询 ====================

    /** 已发布文章分页（isTop 优先，发布时间倒序）。 */
    public IPage<PostVO> pageArticles(long page, long size, Long categoryId, Long tagId) {
        Page<BlogPost> p = Page.of(page, Math.min(size, 100));
        IPage<BlogPost> result;
        if (categoryId != null) {
            result = postMapper.selectByCategory(p, categoryId);
        } else if (tagId != null) {
            result = postMapper.selectByTag(p, tagId);
        } else {
            result = postMapper.selectPage(p, new LambdaQueryWrapper<BlogPost>()
                    .eq(BlogPost::getType, Constants.TYPE_ARTICLE)
                    .eq(BlogPost::getStatus, Constants.STATUS_PUBLISHED)
                    .orderByDesc(BlogPost::getIsTop)
                    .orderByDesc(BlogPost::getPublishedAt));
        }
        return fillRelations(result.convert(PostVO::from), false);
    }

    /** 详情：id 或 slug；密码文章需 password 匹配才返回正文。返回 data={post,prev,next}。 */
    public Map<String, Object> detail(String idOrSlug, String password) {
        BlogPost post = null;
        if (StringUtils.hasText(idOrSlug) && idOrSlug.chars().allMatch(Character::isDigit)) {
            post = postMapper.selectById(Long.valueOf(idOrSlug));
        }
        if (post == null) {
            post = lookupBySlug(idOrSlug);
        }
        if (post == null || post.getStatus() == Constants.STATUS_DRAFT) {
            throw new BizException(404, "内容不存在");
        }
        PostVO vo = PostVO.from(post);
        // 迁移数据只有 content_md，这里兜底渲染
        if (!StringUtils.hasText(vo.getContentHtml()) && StringUtils.hasText(vo.getContentMd())) {
            vo.setContentHtml(MdConverter.toHtml(vo.getContentMd()));
        }
        if (post.getStatus() == Constants.STATUS_PASSWORD && post.getType() != Constants.TYPE_SHUOSHUO) {
            if (!StringUtils.hasText(password) || !password.equals(post.getPassword())) {
                vo.setContentMd(null);
                vo.setContentHtml(null);
                vo.setPassword(null);
                Map<String, Object> data = new HashMap<>();
                data.put("post", vo);
                data.put("passwordRequired", true);
                return data;
            }
            vo.setPassword(null);
        }
        fillRelations(List.of(vo));
        Map<String, Object> data = new HashMap<>();
        data.put("post", vo);
        data.put("passwordRequired", false);
        if (post.getType() == Constants.TYPE_ARTICLE && post.getPublishedAt() != null) {
            BlogPost prev = postMapper.selectPrev(post.getPublishedAt());
            BlogPost next = postMapper.selectNext(post.getPublishedAt());
            data.put("prev", prev == null ? null : Map.of("id", prev.getId(), "title", prev.getTitle(), "slug", prev.getSlug()));
            data.put("next", next == null ? null : Map.of("id", next.getId(), "title", next.getTitle(), "slug", next.getSlug()));
        }
        return data;
    }

    /** 相关文章（同分类/标签）。 */
    public List<PostVO> related(Long id) {
        return postMapper.selectRelated(id).stream()
                .map(p -> PostVO.from(p).stripContent())
                .collect(Collectors.toList());
    }

    /** 浏览量 +1（IP+UA 24h 去重）。 */
    public Map<String, Integer> incrViews(Long id, String dedupKey) {
        BlogPost post = postMapper.selectById(id);
        if (post == null) {
            throw new BizException(404, "内容不存在");
        }
        String key = Constants.CACHE_VIEW + id + ":" + dedupKey;
        if (!kvCache.has(key)) {
            kvCache.put(key, 1, Constants.TTL_VIEW_DEDUP);
            BlogPost upd = new BlogPost();
            upd.setId(id);
            upd.setViews(post.getViews() + 1);
            postMapper.updateById(upd);
            post.setViews(post.getViews() + 1);
        }
        return Map.of("views", post.getViews());
    }

    /** slug 精确查找，未命中时尝试 URL 解码兜底（WP slug 存量可能是百分号编码）。 */
    private BlogPost lookupBySlug(String slug) {
        BlogPost p = postMapper.selectOne(new LambdaQueryWrapper<BlogPost>()
                .eq(BlogPost::getSlug, slug).last("LIMIT 1"));
        if (p == null && slug.contains("%")) {
            try {
                String decoded = java.net.URLDecoder.decode(slug, java.nio.charset.StandardCharsets.UTF_8);
                p = postMapper.selectOne(new LambdaQueryWrapper<BlogPost>()
                        .eq(BlogPost::getSlug, decoded).last("LIMIT 1"));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return p;
    }

    /** 单页（type=1）。 */
    public PostVO pageBySlug(String slug) {
        BlogPost post = lookupBySlug(slug);
        if (post == null || post.getType() != Constants.TYPE_PAGE || post.getStatus() == Constants.STATUS_DRAFT) {
            throw new BizException(404, "页面不存在");
        }
        PostVO vo = PostVO.from(post);
        if (!StringUtils.hasText(vo.getContentHtml()) && StringUtils.hasText(vo.getContentMd())) {
            vo.setContentHtml(MdConverter.toHtml(vo.getContentMd()));
        }
        fillRelations(List.of(vo));
        return vo;
    }

    /** 归档：按年分组。 */
    public List<Map<String, Object>> archives() {
        List<BlogPost> posts = postMapper.selectList(new LambdaQueryWrapper<BlogPost>()
                .eq(BlogPost::getType, Constants.TYPE_ARTICLE)
                .eq(BlogPost::getStatus, Constants.STATUS_PUBLISHED)
                .orderByDesc(BlogPost::getPublishedAt));
        Map<String, List<Map<String, Object>>> byYear = new LinkedHashMap<>();
        for (BlogPost p : posts) {
            LocalDateTime t = p.getPublishedAt() != null ? p.getPublishedAt() : p.getCreateTime();
            String year = t == null ? "未知" : String.valueOf(t.getYear());
            byYear.computeIfAbsent(year, k -> new ArrayList<>())
                    .add(Map.of("id", p.getId(), "title", p.getTitle(), "slug",
                            p.getSlug() == null ? String.valueOf(p.getId()) : p.getSlug(),
                            "createTime", t == null ? "" : t.format(DT)));
        }
        return byYear.entrySet().stream()
                .map(e -> Map.<String, Object>of("year", e.getKey(), "posts", e.getValue()))
                .collect(Collectors.toList());
    }

    /** 搜索：FULLTEXT 优先，异常/空结果回退 LIKE。 */
    public IPage<PostVO> search(String q, long page, long size, List<Integer> types) {
        Page<BlogPost> p = Page.of(page, Math.min(size, 100));
        IPage<BlogPost> result = null;
        try {
            result = postMapper.selectFulltext(p, q, types);
        } catch (Exception ignored) {
            // ngram 不可用等情况回退 LIKE
        }
        if (result == null || result.getRecords().isEmpty()) {
            LambdaQueryWrapper<BlogPost> w = new LambdaQueryWrapper<BlogPost>()
                    .eq(BlogPost::getStatus, Constants.STATUS_PUBLISHED)
                    .in(BlogPost::getType, types)
                    .and(q1 -> q1.like(BlogPost::getTitle, q).or().like(BlogPost::getContentMd, q))
                    .orderByDesc(BlogPost::getPublishedAt);
            result = postMapper.selectPage(p, w);
        }
        return fillRelations(result.convert(PostVO::from), false);
    }

    // ==================== 管理端 ====================

    public IPage<PostVO> adminPage(long page, long size, Integer type, Integer status, String keyword) {
        LambdaQueryWrapper<BlogPost> w = new LambdaQueryWrapper<BlogPost>()
                .eq(type != null, BlogPost::getType, type)
                .eq(status != null, BlogPost::getStatus, status)
                .like(StringUtils.hasText(keyword), BlogPost::getTitle, keyword)
                .orderByDesc(BlogPost::getUpdateTime);
        IPage<PostVO> voPage = postMapper.selectPage(Page.of(page, Math.min(size, 100)), w).convert(PostVO::from);
        // 管理列表保留 md 便于快速查看，但不返回渲染 html
        voPage.getRecords().forEach(v -> v.setContentHtml(null));
        return fillRelations(voPage, true);
    }

    public PostVO adminDetail(Long id) {
        BlogPost post = postMapper.selectById(id);
        if (post == null) {
            throw new BizException(404, "内容不存在");
        }
        PostVO vo = PostVO.from(post);
        fillRelations(List.of(vo));
        List<BlogPostCategory> pcs = postCategoryMapper.selectList(
                new LambdaQueryWrapper<BlogPostCategory>().eq(BlogPostCategory::getPostId, id));
        List<BlogPostTag> pts = postTagMapper.selectList(
                new LambdaQueryWrapper<BlogPostTag>().eq(BlogPostTag::getPostId, id));
        vo.setCategoryIds(pcs.stream().map(BlogPostCategory::getCategoryId).collect(Collectors.toList()));
        vo.setTagIds(pts.stream().map(BlogPostTag::getTagId).collect(Collectors.toList()));
        return vo;
    }

    @Transactional
    public PostVO create(PostSaveDTO dto, LoginPrincipal user) {
        BlogPost post = new BlogPost();
        applyDto(post, dto);
        post.setAuthorId(user == null ? 1L : user.getId());
        if (post.getStatus() == Constants.STATUS_PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        post.setViews(0);
        post.setUpvotes(0);
        post.setCommentCount(0);
        try {
            postMapper.insert(post);
        } catch (DuplicateKeyException e) {
            throw new BizException("slug 已存在，请更换");
        }
        saveRelations(post.getId(), dto);
        return adminDetail(post.getId());
    }

    @Transactional
    public PostVO update(Long id, PostSaveDTO dto) {
        BlogPost post = postMapper.selectById(id);
        if (post == null) {
            throw new BizException(404, "内容不存在");
        }
        applyDto(post, dto);
        if (post.getStatus() == Constants.STATUS_PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        try {
            postMapper.updateById(post);
        } catch (DuplicateKeyException e) {
            throw new BizException("slug 已存在，请更换");
        }
        postCategoryMapper.delete(new LambdaQueryWrapper<BlogPostCategory>().eq(BlogPostCategory::getPostId, id));
        postTagMapper.delete(new LambdaQueryWrapper<BlogPostTag>().eq(BlogPostTag::getPostId, id));
        saveRelations(id, dto);
        return adminDetail(id);
    }

    @Transactional
    public void delete(Long id) {
        postMapper.deleteById(id);
        postCategoryMapper.delete(new LambdaQueryWrapper<BlogPostCategory>().eq(BlogPostCategory::getPostId, id));
        postTagMapper.delete(new LambdaQueryWrapper<BlogPostTag>().eq(BlogPostTag::getPostId, id));
    }

    /** 内容渲染：md -> html。 */
    private void applyDto(BlogPost post, PostSaveDTO dto) {
        post.setType(dto.getType());
        post.setTitle(dto.getTitle());
        post.setSlug(dto.getSlug());
        post.setSummary(dto.getSummary());
        post.setCover(dto.getCover());
        post.setContentMd(dto.getContentMd());
        post.setContentHtml(MdConverter.toHtml(dto.getContentMd()));
        post.setStatus(dto.getStatus() == null ? Constants.STATUS_DRAFT : dto.getStatus());
        post.setPassword(dto.getPassword());
        post.setIsTop(dto.getIsTop() == null ? 0 : dto.getIsTop());
        post.setAllowComment(dto.getAllowComment() == null ? 1 : dto.getAllowComment());
        if (StringUtils.hasText(dto.getPublishedAt())) {
            post.setPublishedAt(LocalDateTime.parse(dto.getPublishedAt().replace(' ', 'T')));
        }
        if (post.getType() == Constants.TYPE_SHUOSHUO) {
            // 说说无 slug，避免唯一键冲突
            post.setSlug(null);
        }
    }

    private void saveRelations(Long postId, PostSaveDTO dto) {
        if (dto.getCategoryIds() != null) {
            for (Long cid : dto.getCategoryIds()) {
                BlogPostCategory pc = new BlogPostCategory();
                pc.setPostId(postId);
                pc.setCategoryId(cid);
                postCategoryMapper.insert(pc);
            }
        }
        if (dto.getTagIds() != null) {
            for (Long tid : dto.getTagIds()) {
                BlogPostTag pt = new BlogPostTag();
                pt.setPostId(postId);
                pt.setTagId(tid);
                postTagMapper.insert(pt);
            }
        }
    }

    /** 批量填充分类/标签/作者昵称。withIds=true 时同时填 categoryIds/tagIds（管理端编辑用）。 */
    public IPage<PostVO> fillRelations(IPage<PostVO> voPage, boolean withIds) {
        List<PostVO> vos = voPage.getRecords();
        fillRelations(vos);
        if (withIds && !vos.isEmpty()) {
            List<Long> ids = vos.stream().map(PostVO::getId).toList();
            Map<Long, List<Long>> catMap = postCategoryMapper.selectList(
                            new LambdaQueryWrapper<BlogPostCategory>().in(BlogPostCategory::getPostId, ids)).stream()
                    .collect(Collectors.groupingBy(BlogPostCategory::getPostId,
                            Collectors.mapping(BlogPostCategory::getCategoryId, Collectors.toList())));
            Map<Long, List<Long>> tagMap = postTagMapper.selectList(
                            new LambdaQueryWrapper<BlogPostTag>().in(BlogPostTag::getPostId, ids)).stream()
                    .collect(Collectors.groupingBy(BlogPostTag::getPostId,
                            Collectors.mapping(BlogPostTag::getTagId, Collectors.toList())));
            for (PostVO vo : vos) {
                vo.setCategoryIds(catMap.getOrDefault(vo.getId(), List.of()));
                vo.setTagIds(tagMap.getOrDefault(vo.getId(), List.of()));
            }
        }
        return voPage;
    }

    public void fillRelations(List<PostVO> vos) {
        if (vos.isEmpty()) {
            return;
        }
        Set<Long> catIds = new HashSet<>();
        Set<Long> tagIds = new HashSet<>();
        Map<Long, List<BlogPostCategory>> catByPost = postCategoryMapper.selectList(
                        new LambdaQueryWrapper<BlogPostCategory>()
                                .in(BlogPostCategory::getPostId, vos.stream().map(PostVO::getId).toList()))
                .stream().collect(Collectors.groupingBy(BlogPostCategory::getPostId));
        Map<Long, List<BlogPostTag>> tagByPost = postTagMapper.selectList(
                        new LambdaQueryWrapper<BlogPostTag>()
                                .in(BlogPostTag::getPostId, vos.stream().map(PostVO::getId).toList()))
                .stream().collect(Collectors.groupingBy(BlogPostTag::getPostId));
        catByPost.values().forEach(l -> l.forEach(pc -> catIds.add(pc.getCategoryId())));
        tagByPost.values().forEach(l -> l.forEach(pt -> tagIds.add(pt.getTagId())));
        Map<Long, BlogCategory> cats = catIds.isEmpty() ? Map.of()
                : categoryMapper.selectBatchIds(catIds).stream().collect(Collectors.toMap(BlogCategory::getId, c -> c));
        Map<Long, BlogTag> tags = tagIds.isEmpty() ? Map.of()
                : tagMapper.selectBatchIds(tagIds).stream().collect(Collectors.toMap(BlogTag::getId, t -> t));
        for (PostVO vo : vos) {
            List<PostVO.IdName> cs = catByPost.getOrDefault(vo.getId(), List.of()).stream()
                    .map(pc -> cats.get(pc.getCategoryId()))
                    .filter(Objects::nonNull)
                    .map(c -> new PostVO.IdName(c.getId(), c.getName(), c.getSlug()))
                    .collect(Collectors.toList());
            List<PostVO.IdName> ts = tagByPost.getOrDefault(vo.getId(), List.of()).stream()
                    .map(pt -> tags.get(pt.getTagId()))
                    .filter(Objects::nonNull)
                    .map(t -> new PostVO.IdName(t.getId(), t.getName(), t.getSlug()))
                    .collect(Collectors.toList());
            vo.setCategories(cs);
            vo.setTags(ts);
        }
        // 作者昵称
        Set<Long> authorIds = vos.stream().map(PostVO::getAuthorId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!authorIds.isEmpty()) {
            Map<Long, String> names = userMapper.selectBatchIds(authorIds).stream()
                    .collect(Collectors.toMap(BlogUser::getId, u -> u.getNickname() == null ? u.getUsername() : u.getNickname()));
            vos.forEach(vo -> vo.setAuthorNickname(names.get(vo.getAuthorId())));
        }
    }
}
