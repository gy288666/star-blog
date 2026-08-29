package com.blog.module.file.service;

import com.blog.common.exception.BizException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** 本地文件存储：{uploadDir}/yyyy/MM/uuid.ext，URL 形如 /uploads/2026/08/xxx.png。 */
@Service
public class FileService {

    private static final Set<String> ALLOWED_EXT = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "svg", "mp4", "mp3", "pdf", "zip", "txt", "md");

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy/MM");

    @Value("${blog.upload-dir}")
    private String uploadDir;

    @Data
    public static class FileItem {
        private String name;
        private String url;
        private long size;
        private String modifyTime;
    }

    public Map<String, String> store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("文件为空");
        }
        String original = Objects.requireNonNullElse(file.getOriginalFilename(), "file");
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException("不支持的文件类型: " + ext);
        }
        String subDir = LocalDate.now().format(MONTH);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dir = Paths.get(uploadDir, subDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BizException(500, "保存文件失败");
        }
        return Map.of("url", "/uploads/" + subDir + "/" + filename, "name", original);
    }

    /** 列出 dir（相对 uploadDir，"" 表示根）下的文件，按修改时间倒序。 */
    public List<FileItem> list(String dir, int page, int size) {
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = base.resolve(Objects.requireNonNullElse(dir, "")).normalize();
        if (!target.startsWith(base)) {
            throw new BizException("非法目录");
        }
        if (!Files.isDirectory(target)) {
            return List.of();
        }
        List<FileItem> items;
        try (Stream<Path> stream = Files.list(target)) {
            items = stream.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing((Path p) -> {
                        try {
                            return Files.getLastModifiedTime(p);
                        } catch (IOException e) {
                            return FileTime.fromMillis(0);
                        }
                    }).reversed())
                    .map(p -> {
                        FileItem item = new FileItem();
                        item.setName(p.getFileName().toString());
                        item.setUrl("/uploads/" + base.relativize(p).toString().replace('\\', '/'));
                        try {
                            item.setSize(Files.size(p));
                            item.setModifyTime(Files.getLastModifiedTime(p).toString().substring(0, 19));
                        } catch (IOException ignored) {
                        }
                        return item;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new BizException(500, "读取目录失败");
        }
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    /** 删除相对路径文件（防目录穿越）。 */
    public void delete(String path) {
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = base.resolve(Objects.requireNonNullElse(path, "")).normalize();
        if (!target.startsWith(base) || !target.equals(base)) {
            throw new BizException("非法路径");
        }
        if (target.equals(base)) {
            throw new BizException("非法路径");
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new BizException(500, "删除失败");
        }
    }
}
