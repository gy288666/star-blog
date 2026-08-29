package com.blog.module.comment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.constant.Constants;
import com.blog.common.exception.BizException;
import com.blog.module.comment.dto.CommentCreateDTO;
import com.blog.module.comment.entity.BlogComment;
import com.blog.module.comment.mapper.CommentMapper;
import com.blog.module.comment.vo.CommentAdminVO;
import com.blog.module.comment.vo.CommentVO;
import com.blog.module.post.entity.BlogPost;
import com.blog.module.post.mapper.PostMapper;
import com.blog.module.setting.service.SettingService;
import com.blog.security.LoginPrincipal;
import com.blog.util.HtmlSanitizer;
import com.blog.util.KVCache;
import com.blog.util.MdConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final SettingService settingService;
    private final KVCache kvCache;

    /** 根评论分页（新在前），children 按 root_id 内嵌（旧在前）。 */
    public IPage<CommentVO> pageByPost(Long postId, long page, long size) {
        Page<BlogComment> p = Page.of(page, Math.min(size, 100));
        IPage<BlogComment> roots = commentMapper.selectPage(p, new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getPostId, postId)
                .eq(BlogComment::getParentId, 0)
                .eq(BlogComment::getStatus, Constants.COMMENT_APPROVED)
                .orderByDesc(BlogComment::getCreateTime).orderByDesc(BlogComment::getId));
        List<CommentVO> vos = roots.getRecords().stream().map(CommentVO::from).collect(Collectors.toList());
        if (!vos.isEmpty()) {
            List<Long> rootIds = vos.stream().map(CommentVO::getId).toList();
            Map<Long, List<CommentVO>> children = commentMapper.selectChildrenByRoots(rootIds).stream()
                    .map(CommentVO::from)
                    .collect(Collectors.groupingBy(CommentVO::getRootId));
            vos.forEach(vo -> vo.setChildren(children.getOrDefault(vo.getId(), List.of())));
        }
        Page<CommentVO> voPage = new Page<>(roots.getCurrent(), roots.getSize(), roots.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }

    /** 游客发表（验证码 + 开关校验）。 */
    @Transactional
    public CommentVO create(CommentCreateDTO dto, String ip, String userAgent) {
        if ("0".equals(settingService.get("allowComment", "1"))) {
            throw new BizException("评论功能已关闭");
        }
        String expect = kvCache.getAndRemove(Constants.CACHE_CAPTCHA + dto.getCaptchaKey());
        if (expect == null || !expect.equalsIgnoreCase(dto.getCaptchaCode())) {
            throw new BizException("验证码错误或已过期");
        }
        BlogPost post = postMapper.selectById(dto.getPostId());
        if (post == null || post.getStatus() == Constants.STATUS_DRAFT) {
            throw new BizException(404, "内容不存在");
        }
        if (post.getAllowComment() != null && post.getAllowComment() == 0) {
            throw new BizException("该内容不允许评论");
        }
        BlogComment c = new BlogComment();
        c.setPostId(dto.getPostId());
        c.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        c.setRootId(dto.getRootId() == null ? 0L : dto.getRootId());
        c.setAuthor(dto.getAuthor());
        c.setEmail(dto.getEmail());
        c.setWebsite(dto.getWebsite());
        c.setContentMd(dto.getContent());
        c.setContentHtml(HtmlSanitizer.clean(MdConverter.toHtml(dto.getContent())));
        c.setIp(ip);
        c.setUserAgent(userAgent == null ? null : userAgent.substring(0, Math.min(500, userAgent.length())));
        c.setStatus(Constants.COMMENT_APPROVED);
        c.setIsAdmin(0);
        // 有父评论时以父评论的 root 归组
        if (c.getParentId() > 0) {
            BlogComment parent = commentMapper.selectById(c.getParentId());
            if (parent == null) {
                throw new BizException("回复的评论不存在");
            }
            c.setRootId(parent.getRootId() == 0 ? parent.getId() : parent.getRootId());
        }
        commentMapper.insert(c);
        incrCommentCount(dto.getPostId(), 1);
        return CommentVO.from(c);
    }

    /** 管理员回复：免验证码，直接通过。 */
    @Transactional
    public CommentVO adminReply(CommentCreateDTO dto, LoginPrincipal user) {
        BlogComment c = new BlogComment();
        c.setPostId(dto.getPostId());
        c.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        c.setRootId(dto.getRootId() == null ? 0L : dto.getRootId());
        c.setAuthor(user == null || user.getNickname() == null ? "站长" : user.getNickname());
        c.setContentMd(dto.getContent());
        c.setContentHtml(HtmlSanitizer.clean(MdConverter.toHtml(dto.getContent())));
        c.setStatus(Constants.COMMENT_APPROVED);
        c.setIsAdmin(1);
        if (c.getParentId() > 0) {
            BlogComment parent = commentMapper.selectById(c.getParentId());
            if (parent == null) {
                throw new BizException("回复的评论不存在");
            }
            c.setRootId(parent.getRootId() == 0 ? parent.getId() : parent.getRootId());
        }
        commentMapper.insert(c);
        incrCommentCount(dto.getPostId(), 1);
        return CommentVO.from(c);
    }

    public IPage<CommentAdminVO> adminPage(long page, long size, Integer status, Long postId) {
        return commentMapper.selectAdminPage(Page.of(page, Math.min(size, 100)), status, postId);
    }

    /** 审核：切换状态时同步冗余计数。 */
    @Transactional
    public void updateStatus(Long id, Integer status) {
        BlogComment c = commentMapper.selectById(id);
        if (c == null) {
            throw new BizException(404, "评论不存在");
        }
        boolean wasApproved = c.getStatus() == Constants.COMMENT_APPROVED;
        c.setStatus(status);
        commentMapper.updateById(c);
        boolean isApproved = status == Constants.COMMENT_APPROVED;
        if (wasApproved != isApproved) {
            incrCommentCount(c.getPostId(), isApproved ? 1 : -1);
        }
    }

    /** 删除评论及其子评论。 */
    @Transactional
    public void delete(Long id) {
        BlogComment c = commentMapper.selectById(id);
        if (c == null) {
            return;
        }
        List<BlogComment> children = commentMapper.selectList(new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getRootId, id));
        int approvedRemoved = 0;
        if (c.getStatus() == Constants.COMMENT_APPROVED) {
            approvedRemoved++;
        }
        approvedRemoved += children.stream().filter(ch -> ch.getStatus() == Constants.COMMENT_APPROVED).count();
        commentMapper.deleteById(id);
        if (!children.isEmpty()) {
            commentMapper.deleteBatchIds(children.stream().map(BlogComment::getId).toList());
        }
        if (approvedRemoved > 0) {
            incrCommentCount(c.getPostId(), -approvedRemoved);
        }
    }

    private void incrCommentCount(Long postId, int delta) {
        BlogPost post = postMapper.selectById(postId);
        if (post == null) {
            return;
        }
        BlogPost upd = new BlogPost();
        upd.setId(postId);
        upd.setCommentCount(Math.max(0, (post.getCommentCount() == null ? 0 : post.getCommentCount()) + delta));
        postMapper.updateById(upd);
    }
}
