-- 已部署环境升级：背景图/封面改由前端静态托管（git pull + 重建前端后执行一次）
-- 背景（前端 public/bg/）
UPDATE blog_setting SET setting_value = '/bg/page-light.webp' WHERE setting_key = 'pageBackgroundImage';
UPDATE blog_setting SET setting_value = '/bg/page-dark.webp'  WHERE setting_key = 'pageBackgroundImageDark';
-- 文章封面轮换
UPDATE blog_post SET cover = CASE id % 3
    WHEN 1 THEN '/bg/cover-1.png'
    WHEN 2 THEN '/bg/cover-2.png'
    ELSE '/bg/cover-3.png' END
WHERE type = 0 AND cover IS NOT NULL;
