package com.blog.module.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.constant.Constants;
import com.blog.common.exception.BizException;
import com.blog.module.post.entity.BlogPost;
import com.blog.module.post.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 文章封面每周刷新：从哲风壁纸(haowallpaper.com)随机选取，每周一凌晨 3 点执行。
 * 壁纸站有 Referer 防盗链，因此刷新时把图片下载到本站 uploads 目录，封面存本地路径。
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
    private static final long MAX_IMAGE_BYTES = 8 * 1024 * 1024;

    private final PostMapper postMapper;
    private final HttpClient http;
    private final boolean enabled;
    private final String uploadDir;
    private final Object downloadLock = new Object();

    public CoverRefreshService(PostMapper postMapper,
                               @Value("${blog.cover-refresh.enabled:true}") boolean enabled,
                               @Value("${blog.upload-dir:./uploads}") String uploadDir) {
        this.postMapper = postMapper;
        this.enabled = enabled;
        this.uploadDir = uploadDir;
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
        // 本轮新封面先落到临时目录，全部成功后统一切换并清理上一轮文件
        Path tmpDir = coversDir().resolveSibling("covers-tmp-" + UUID.randomUUID().toString().substring(0, 8));
        List<String> localCovers = new ArrayList<>();
        int cursor = 0;
        try {
            for (BlogPost p : posts) {
                String current = p.getCover();
                for (int attempt = 0; attempt < 5; attempt++) {
                    String id = pool.get(cursor % pool.size());
                    cursor++;
                    String remote = String.format(COVER_URL_FMT, id);
                    if (remote.equals(current)) {
                        continue;
                    }
                    try {
                        Path saved = downloadTo(remote, tmpDir);
                        // 临时目录随后整体转正为 covers/，故最终 URL 直接用文件名
                        localCovers.add("/uploads/covers/" + saved.getFileName());
                        break;
                    } catch (Exception e) {
                        log.warn("封面下载失败，换下一张: {}", e.getMessage());
                    }
                }
            }
            if (localCovers.size() < posts.size() / 2) {
                throw new IllegalStateException("可用封面过少（" + localCovers.size() + "/" + posts.size() + "），放弃本轮");
            }
        } finally {
            if (localCovers.size() < posts.size() / 2) {
                deleteQuietly(tmpDir);
            }
        }

        // 应用：每个文章按顺序取一个本地封面；清空旧 covers 目录；临时目录转正
        Path officialDir = coversDir();
        int updated = 0;
        List<BlogPost> published = postMapper.selectList(new LambdaQueryWrapper<BlogPost>()
                .eq(BlogPost::getType, Constants.TYPE_ARTICLE)
                .eq(BlogPost::getStatus, Constants.STATUS_PUBLISHED));
        for (int i = 0; i < published.size() && i < localCovers.size(); i++) {
            BlogPost upd = new BlogPost();
            upd.setId(published.get(i).getId());
            upd.setCover(localCovers.get(i));
            postMapper.updateById(upd);
            updated++;
        }
        // tmp 目录重命名为正式目录（先移走旧目录做备份式清理）
        synchronized (downloadLock) {
            deleteQuietly(officialDir);
            try {
                Files.createDirectories(officialDir.getParent());
                Files.move(tmpDir, officialDir, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new BizException(500, "封面目录切换失败: " + e.getMessage());
            }
        }
        return updated;
    }

    /** 下载远程图片到 dir，返回保存的文件路径。校验图片类型与大小。 */
    private Path downloadTo(String url, Path dir) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", UA)
                // 站点对无此头的请求也会强制 gzip，而 HttpClient 不自动解压，必须显式关掉
                .header("Accept-Encoding", "identity")
                .GET().build();
        HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() != 200) {
            throw new IOException("HTTP " + resp.statusCode());
        }
        byte[] body = resp.body();
        // 兜底：若仍收到 gzip 流（魔数 1f 8b）则解压
        if (body.length > 2 && (body[0] & 0xff) == 0x1f && (body[1] & 0xff) == 0x8b) {
            body = gunzip(body);
        }
        String ctype = resp.headers().firstValue("Content-Type").orElse("");
        if (!ctype.startsWith("image/") || body.length < 5000 || body.length > MAX_IMAGE_BYTES) {
            throw new IOException("非有效图片: " + ctype + " " + body.length + "B");
        }
        Files.createDirectories(dir);
        // 站点的 Content-Type 不可靠（webp 也报 jpeg），按文件魔数定扩展名
        String ext = sniffImageExt(body);
        Path target = dir.resolve(UUID.randomUUID().toString().replace("-", "") + ext);
        Files.write(target, body);
        return target;
    }

    private String sniffImageExt(byte[] b) {
        if (b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return ".webp";
        }
        if (b.length > 3 && (b[0] & 0xff) == 0x89 && b[1] == 'P') {
            return ".png";
        }
        return ".jpg";
    }

    private byte[] gunzip(byte[] data) throws IOException {
        try (java.util.zip.GZIPInputStream in = new java.util.zip.GZIPInputStream(
                new java.io.ByteArrayInputStream(data))) {
            return in.readAllBytes();
        }
    }

    private Path coversDir() {
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve("covers");
    }

    private void deleteQuietly(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    /** 抓取站点首页 + 一个详情页，汇总候选壁纸 id。 */
    private List<String> fetchCandidateIds() {
        List<String> ids = new ArrayList<>();
        String home = fetch(SITE + "/");
        collectIds(home, ids);
        if (ids.size() < 8 && home != null) {
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
