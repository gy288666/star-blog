-- 种子：关于页 + QQ 群友链（幂等，可重复执行）

-- 关于页（type=1 单页，后台编辑器可继续修改；id 用高位避免与自增文章冲突）
INSERT IGNORE INTO blog_post
    (id, type, title, slug, content_md, status, author_id, views, upvotes, is_top, allow_comment, comment_count)
VALUES
    (900001, 1, '关于', 'about',
     '## 关于 Star\n\n这里是 **Star**，一个记录 AI、编程与日常碎碎念的个人小站。\n\n- 写技术笔记，也写生活随想\n- 相信 AI 是协作伙伴而不是替代者\n- 站点由 Spring Boot 3 + Nuxt 3 全栈搭建，看板娘 Doro 常驻右下角\n\n## 联系我\n\n- **QQ 群**：[聊天互赞扩列群（点此加入）](https://qm.qq.com/q/cqBDMznVbq)\n- **个人 QQ**：修改本页即可填写（后台 → 文章管理 → 关于）\n\n> 月光还是少年的月光，九州一色还是李白的霜。',
     1, 1, 0, 0, 0, 1, 0);

-- QQ 群友链（无唯一键，用 NOT EXISTS 幂等）
INSERT INTO blog_friend (name, url, avatar, description, sort_order, status)
SELECT '聊天互赞扩列群', 'https://qm.qq.com/q/cqBDMznVbq', '/favicon.svg',
       '本站官方 QQ 群：聊天互赞扩列，欢迎来玩', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM blog_friend WHERE url = 'https://qm.qq.com/q/cqBDMznVbq');
