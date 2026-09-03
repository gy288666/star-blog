-- 关于页填入站长 QQ（幂等：仅当仍是占位文案时替换）
UPDATE blog_post
SET content_md = REPLACE(content_md,
    '- **个人 QQ**：修改本页即可填写（后台 → 文章管理 → 关于）',
    '- **个人 QQ**：2855629937')
WHERE slug = 'about' AND content_md LIKE '%修改本页即可填写%';
