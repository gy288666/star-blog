package com.blog.module.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.constant.Constants;
import com.blog.module.post.entity.BlogPost;
import com.blog.module.post.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文章封面每周刷新：从哲风壁纸(haowallpaper.com)随机选取，每周一凌晨 3 点执行。
 * 站点为 Nuxt SSR，列表/详情页 HTML 内含 previewFileImg 文件 id，直接提取。
 * 任一环节失败保留旧封面，不影响站点运行。
 */
@Slf4j
@Service
public class CoverRefreshService {

    private static final Pattern ID_PATTERN =
            Pattern.compile("(?:getCroppingImg|previewFileImg)/(\\d{15,20})");
    private static final String SITE = "https://haowallpaper.com";
    private static final String COVER_URL_FMT = SITE + "/link/common/file/previewFileImg/%s";
    private static final String UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126 Safari/537.36";

    private final PostMapper postMapper;
    private final HttpClient http;
    private final boolean enabled;

    public CoverRefreshService(PostMapper postMapper,
                               @Value("${blog.cover-refresh.enabled:true}") boolean enabled) {
        this.postMapper = postMapper;
        this.enabled = enabled;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /** 每周一 03:00（Asia/Shanghai）自动刷新。 */
    @Scheduled(cron = "${blog.cover-refresh.cron:0 0 3 ? * MON}", zone = "Asia/Shanghai")
    public void weeklyRefresh() {
        if (!enabled) {
            return;
        }
        try {
            int n = refresh();
            log.info("每周封面刷新完成，更新 {} 篇文章", n);
        } catch (Exception e) {
            log.warn("每周封面刷新失败（保留旧封面）: {}", e.getMessage());
        }
    }

    /** 手动触发（管理端按钮），返回更新数量。 */
    public int refresh() {
        List<String> pool = fetchCandidateIds();
        if (pool.size() < 3) {
            throw new IllegalStateException("壁纸候选不足（" + pool.size() + "），站点结构可能变化");
        }
        List<BlogPost> posts = postMapper.selectList(new LambdaQueryWrapper<BlogPost>()
                .eq(BlogPost::getType, Constants.TYPE_ARTICLE)
                .eq(BlogPost::getStatus, Constants.STATUS_PUBLISHED));
        Collections.shuffle(pool);
        int updated = 0;
        int cursor = 0;
        for (BlogPost p : posts) {
            String current = p.getCover();
            // 尝试从池中取一个与当前不同的封面（最多试 5 次）
            for (int attempt = 0; attempt < 5; attempt++) {
                String candidate = String.format(COVER_URL_FMT, pool.get(cursor % pool.size()));
                cursor++;
                if (!candidate.equals(current)) {
                    BlogPost upd = new BlogPost();
                    upd.setId(p.getId());
                    upd.setCover(candidate);
                    postMapper.updateById(upd);
                    updated++;
                    break;
                }
            }
        }
        return updated;
    }

    /** 抓取站点首页 + 一个详情页，汇总候选壁纸 id。 */
    private List<String> fetchCandidateIds() {
        List<String> ids = new ArrayList<>();
        String home = fetch(SITE + "/");
        collectIds(home, ids);
        if (ids.size() < 8 && home != null) {
            // 从首页挑一个详情入口深入抓取
            Matcher m = Pattern.compile("/homeViewLook/(\\d{15,20})").matcher(home);
            if (m.find()) {
                String detail = fetch(SITE + "/homeViewLook/" + m.group(1));
                collectIds(detail, ids);
            }
        }
        return ids;
    }

    private void collectIds(String html, List<String> out) {
        if (html == null) {
            return;
        }
        Matcher m = ID_PATTERN.matcher(html);
        while (m.find()) {
            String id = m.group(1);
            if (!out.contains(id)) {
                out.add(id);
            }
        }
    }

    private String fetch(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", UA)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return resp.body();
            }
            log.warn("抓取壁纸列表返回 {}: {}", resp.statusCode(), url);
        } catch (Exception e) {
            log.warn("抓取壁纸列表失败 {}: {}", url, e.getMessage());
        }
        return null;
    }
}
