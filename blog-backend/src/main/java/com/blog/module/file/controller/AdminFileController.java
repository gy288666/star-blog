package com.blog.module.file.controller;

import com.blog.common.result.Result;
import com.blog.module.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** 管理端文件上传/列表/删除。 */
@RestController
@RequestMapping("/api/admin/files")
@RequiredArgsConstructor
public class AdminFileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(fileService.store(file));
    }

    @GetMapping
    public Result<List<FileService.FileItem>> list(@RequestParam(defaultValue = "") String dir,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "50") int size) {
        return Result.ok(fileService.list(dir, page, Math.min(Math.max(size, 1), 200)));
    }

    @DeleteMapping
    public Result<Void> delete(@RequestParam String path) {
        fileService.delete(path);
        return Result.ok();
    }
}
