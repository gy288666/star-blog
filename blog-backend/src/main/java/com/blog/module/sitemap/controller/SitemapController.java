package com.blog.module.sitemap.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.constant.Constants;
import com.blog.common.result.Result;
import com.blog.module.category.entity.BlogCategory;
import com.blog.module.category.mapper.CategoryMapper;
import com.blog.module.post.entity.BlogPost;
import com.blog.module.post.mapper.PostMapper;
import com.blog.module.post.service.PostService;
import com.blog.module.setting.service.SettingService;
import com.blog.module.tag.entity.BlogTag;
import com.blog.module.tag.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/** sitemap.xml 与归档接口。 */
@RestController
@RequiredArgsConstructor
public class SitemapController {

    private static final DateTimeFormatter W3C = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PostMapper postMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final SettingService settingService;
    private final PostService postService;

    @GetMapping(value = "/api/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        String site = settingService.get("siteUrl", Constants.DEFAULT_SITE_URL).replaceAll("/+$", "");
        StringJoiner urls = new StringJoiner("\n");
        urls.add(entry(site, null, "1.0", "daily"));
        List<BlogPost> posts = postMapper.selectList(new LambdaQueryWrapper<BlogPost>()
                .in(BlogPost::getType, List.of(Constants.TYPE_ARTICLE, Constants.TYPE_PAGE, Constants.TYPE_SHUOSHUO))
                .eq(BlogPost::getStatus, Constants.STATUS_PUBLISHED));
        for (BlogPost p : posts) {
            String loc = switch (p.getType()) {
                case Constants.TYPE_PAGE -> site + "/page/" + p.getSlug();
                case Constants.TYPE_SHUOSHUO -> site + "/shuoshuo";
                default -> site + "/post/" + (p.getSlug() == null ? p.getId() : p.getSlug());
            };
            urls.add(entry(loc, lastmod(p), "0.8", "weekly"));
        }
        for (BlogCategory c : categoryMapper.selectList(null)) {
            if (c.getSlug() != null && !c.getSlug().isBlank()) {
                urls.add(entry(site + "/category/" + c.getSlug(), null, "0.6", "weekly"));
            }
        }
        for (BlogTag t : tagMapper.selectList(null)) {
            if (t.getSlug() != null && !t.getSlug().isBlank()) {
                urls.add(entry(site + "/tag/" + t.getSlug(), null, "0.4", "weekly"));
            }
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
                urls + "\n</urlset>";
    }

    private String lastmod(BlogPost p) {
        LocalDateTime t = p.getUpdateTime() != null ? p.getUpdateTime() : p.getCreateTime();
        return t == null ? null : t.format(W3C);
    }

    private String entry(String loc, String lastmod, String priority, String freq) {
        return "  <url><loc>" + escape(loc) + "</loc>" +
                (lastmod == null ? "" : "<lastmod>" + lastmod + "</lastmod>") +
                "<changefreq>" + freq + "</changefreq><priority>" + priority + "</priority></url>";
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @GetMapping("/api/archives")
    public Result<List<Map<String, Object>>> archives() {
        return Result.ok(postService.archives());
    }
}
