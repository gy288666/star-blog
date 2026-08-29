package com.blog.module.stat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.constant.Constants;
import com.blog.module.comment.entity.BlogComment;
import com.blog.module.comment.mapper.CommentMapper;
import com.blog.module.friend.entity.BlogFriend;
import com.blog.module.friend.mapper.FriendMapper;
import com.blog.module.post.entity.BlogPost;
import com.blog.module.post.mapper.PostMapper;
import com.blog.module.stat.entity.BlogVisitLog;
import com.blog.module.stat.mapper.VisitLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** 仪表盘统计。 */
@Service
@RequiredArgsConstructor
public class StatService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final FriendMapper friendMapper;
    private final VisitLogMapper visitLogMapper;

    public Map<String, Object> summary() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("postCount", postMapper.selectCount(new LambdaQueryWrapper<BlogPost>()
                .eq(BlogPost::getType, Constants.TYPE_ARTICLE)
                .ne(BlogPost::getStatus, -1)));
        out.put("shuoshuoCount", postMapper.selectCount(new LambdaQueryWrapper<BlogPost>()
                .eq(BlogPost::getType, Constants.TYPE_SHUOSHUO)));
        out.put("commentCount", commentMapper.selectCount(new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getStatus, Constants.COMMENT_APPROVED)));
        out.put("pendingComments", commentMapper.selectCount(new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getStatus, Constants.COMMENT_PENDING)));
        List<Object> viewsSum = postMapper.selectObjs(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BlogPost>()
                .select("IFNULL(SUM(views), 0) AS s"));
        out.put("viewsSum", viewsSum.isEmpty() || viewsSum.get(0) == null ? 0 : ((Number) viewsSum.get(0)).longValue());
        out.put("friendCount", friendMapper.selectCount(new LambdaQueryWrapper<BlogFriend>().eq(BlogFriend::getStatus, 1)));
        List<Object> firstDay = postMapper.selectObjs(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BlogPost>()
                .select("IFNULL(MIN(create_time), NOW()) AS d"));
        LocalDate first = Optional.ofNullable(firstDay.isEmpty() ? null : firstDay.get(0))
                .map(Object::toString).map(s -> s.substring(0, Math.min(10, s.length())))
                .map(LocalDate::parse).orElse(LocalDate.now());
        out.put("runDays", Math.max(1, LocalDate.now().toEpochDay() - first.toEpochDay() + 1));
        return out;
    }

    /** 最近 N 天访问趋势（缺失日期补 0）。 */
    public List<Map<String, Object>> viewsTrend(int days) {
        days = Math.min(Math.max(days, 1), 90);
        LocalDate start = LocalDate.now().minusDays(days - 1);
        Map<String, Long> counts = visitLogMapper.selectDailyTrend(start.atStartOfDay()).stream()
                .collect(Collectors.toMap(m -> String.valueOf(m.get("vdate")),
                        m -> ((Number) m.get("cnt")).longValue()));
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            out.add(Map.of("date", d.toString(), "count", counts.getOrDefault(d.toString(), 0L)));
        }
        return out;
    }

    /** 浏览量 Top 文章。 */
    public List<Map<String, Object>> topPosts(int limit) {
        limit = Math.min(Math.max(limit, 1), 50);
        return postMapper.selectList(new LambdaQueryWrapper<BlogPost>()
                        .eq(BlogPost::getType, Constants.TYPE_ARTICLE)
                        .eq(BlogPost::getStatus, Constants.STATUS_PUBLISHED)
                        .orderByDesc(BlogPost::getViews).last("LIMIT " + limit)).stream()
                .map(p -> Map.<String, Object>of("id", p.getId(), "title", p.getTitle(),
                        "views", p.getViews() == null ? 0 : p.getViews()))
                .collect(Collectors.toList());
    }

    public void recordVisit(String ip, String url, String referer, String userAgent) {
        BlogVisitLog log = new BlogVisitLog();
        log.setIp(ip);
        log.setUrl(url == null ? null : url.substring(0, Math.min(500, url.length())));
        log.setReferer(referer == null ? null : referer.substring(0, Math.min(500, referer.length())));
        log.setUserAgent(userAgent == null ? null : userAgent.substring(0, Math.min(500, userAgent.length())));
        log.setVisitTime(LocalDateTime.now());
        visitLogMapper.insert(log);
    }
}
