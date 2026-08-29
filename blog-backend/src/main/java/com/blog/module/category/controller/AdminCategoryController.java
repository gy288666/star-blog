package com.blog.module.category.controller;

import com.blog.common.result.Result;
import com.blog.module.category.entity.BlogCategory;
import com.blog.module.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 管理端分类 CRUD。 */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryService.CategoryVO>> list() {
        return Result.ok(categoryService.listWithCount());
    }

    @PostMapping
    public Result<BlogCategory> save(@RequestBody BlogCategory category) {
        return Result.ok(categoryService.save(category));
    }

    /** 新建与更新合一：有 id 更新，无 id 插入。 */
    @PutMapping("/{id}")
    public Result<BlogCategory> update(@PathVariable Long id, @RequestBody BlogCategory category) {
        category.setId(id);
        return Result.ok(categoryService.save(category));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
