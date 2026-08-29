# blog-backend — Spring Boot 博客后端

WordPress 个人博客重构的后端（原方案文档见项目根目录 `docs/`）。

## 技术栈

- Java 21 + Spring Boot 3.3（Web/Security/Validation）
- MyBatis-Plus 3.5.7（分页插件已配）
- Flyway（建表）+ MySQL 8（utf8mb4，全文搜索用 ngram 解析器）
- jjwt 0.12（HS256，7 天有效期）
- flexmark（Markdown 渲染/迁移时 HTML→MD）+ jsoup（评论 HTML 净化）
- 无 Redis：验证码、浏览量去重等使用进程内 TTL 缓存 `KVCache`（单实例部署足够）

## 启动

```bash
# 开发（默认 profile=dev，连接 localhost:3306，root/root，自动建库）
mvn spring-boot:run

# 生产
JWT_SECRET=<随机串> DB_HOST=... DB_USER=... DB_PASSWORD=... UPLOAD_DIR=/data/uploads \
  java -jar target/blog-backend-1.0.0.jar --spring.profiles.active=prod
```

首次启动会：

1. Flyway 执行 `src/main/resources/db/migration/V1__schema.sql` 建全部 12 张表
2. `SeedDataRunner` 创建管理员 **admin / admin123**（请登录后立即修改）

## 配置项

| 配置 | 环境变量 | 默认 |
|------|----------|------|
| `blog.jwt-secret` | `JWT_SECRET` | dev-only（生产必改） |
| `blog.upload-dir` | `UPLOAD_DIR` | `./uploads` |
| `blog.site-url` | `SITE_URL` | `https://blog.20260006.xyz` |
| `blog.jwt-expire` | - | 604800000 (7天) |
| 数据源 | `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD` | 见 application-prod.yml |

上传文件静态映射：`/uploads/**` → `{upload-dir}` 目录。

## API 一览（完整契约见 `docs/api-contract.md`）

**公开**：`GET /api/posts`、`GET /api/posts/{idOrSlug}`（`?password=`）、`GET /api/posts/{id}/related`、
`PUT /api/posts/{id}/views`、`GET /api/pages/{slug}`、`GET /api/categories`、`GET /api/tags`、
`GET /api/comments?postId=`、`POST /api/comments`（需验证码）、`GET /api/shuoshuos`、
`POST /api/shuoshuos/{id}/like`、`GET /api/shuoshuos/{id}/comments`、`GET /api/search?q=`、
`GET /api/banners`、`GET /api/friends`、`GET /api/settings/public`、`POST /api/auth/login`、
`GET /api/auth/me`、`GET /api/captcha`、`POST /api/visit`、`GET /api/archives`、`GET /api/sitemap.xml`

**管理（Bearer Token，ADMIN 角色）**：`/api/admin/posts`、`/categories`、`/tags`、`/comments`（含 `{id}/status` 审核与管理员回复）、
`/friends`、`/banners`、`/settings`、`/files/upload`、`/files`、`/stats/summary`、`/stats/views?days=`、`/stats/topPosts`、
`/password`、`/profile`

## 设计要点

- **统一返回** `{code, msg, data}`，code=0 成功；HTTP 状态恒为 200（静态资源 404 除外）
- **分页** `{records, total, page, size}`（`PageVO`）
- **浏览量/点赞去重**：SHA256(ip+ua)，浏览量内存去重 24h；说说点赞靠 `blog_shuoshuo_like` 唯一键
- **密码保护文章**：status=2，前端带 `?password=` 明文比对，不匹配时不返回正文（`passwordRequired:true`）
- **评论**：两级展示（root_id 归组），内容 md→flexmark 渲染→jsoup 白名单净化后存 `content_html`；
  冗余 `comment_count` 随审核/删除同步维护
- **全文搜索**：`MATCH ... AGAINST(ngram)` 失败或空结果回退 `LIKE`

## 测试

```bash
mvn test   # MdConverter / HtmlSanitizer / JwtUtil 共 11 个用例
```
