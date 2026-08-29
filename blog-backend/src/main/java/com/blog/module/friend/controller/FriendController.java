package com.blog.module.friend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.result.Result;
import com.blog.module.friend.entity.BlogFriend;
import com.blog.module.friend.mapper.FriendMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 公开友链。 */
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendMapper friendMapper;

    @GetMapping
    public Result<List<BlogFriend>> list() {
        return Result.ok(friendMapper.selectList(new LambdaQueryWrapper<BlogFriend>()
                .eq(BlogFriend::getStatus, 1)
                .orderByAsc(BlogFriend::getSortOrder).orderByAsc(BlogFriend::getId)));
    }
}
