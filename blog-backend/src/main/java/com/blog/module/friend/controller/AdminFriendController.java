package com.blog.module.friend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.result.Result;
import com.blog.module.friend.entity.BlogFriend;
import com.blog.module.friend.mapper.FriendMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 管理端友链 CRUD。 */
@RestController
@RequestMapping("/api/admin/friends")
@RequiredArgsConstructor
public class AdminFriendController {

    private final FriendMapper friendMapper;

    @GetMapping
    public Result<List<BlogFriend>> list() {
        return Result.ok(friendMapper.selectList(
                new LambdaQueryWrapper<BlogFriend>().orderByAsc(BlogFriend::getSortOrder).orderByAsc(BlogFriend::getId)));
    }

    @PostMapping
    public Result<BlogFriend> save(@RequestBody BlogFriend friend) {
        if (friend.getId() == null) {
            friendMapper.insert(friend);
        } else {
            friendMapper.updateById(friend);
        }
        return Result.ok(friend);
    }

    @PutMapping("/{id}")
    public Result<BlogFriend> update(@PathVariable Long id, @RequestBody BlogFriend friend) {
        friend.setId(id);
        friendMapper.updateById(friend);
        return Result.ok(friend);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        friendMapper.deleteById(id);
        return Result.ok();
    }
}
