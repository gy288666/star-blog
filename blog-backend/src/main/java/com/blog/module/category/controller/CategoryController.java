package com.blog.module.category.controller;

import com.blog.common.result.Result;
import com.blog.module.category.entity.BlogCategory;
import com.blog.module.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 公开分类列表。 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryService.CategoryVO>> list() {
        return Result.ok(categoryService.listWithCount());
    }
}
