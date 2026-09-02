# 个人博客重构：WordPress → Spring Boot 3 + Nuxt 3

原 WordPress（Argon 主题）个人博客的完整重构，替代臃肿的 PHP 架构。

## 项目结构

```
├── blog-backend/        Spring Boot 3.3 + Java 21 + MyBatis-Plus + MySQL 8（REST API）
├── blog-frontend/       Nuxt 3 SSR 博客 + /admin 管理后台 SPA（Element Plus）
├── deploy/              Docker Compose + Nginx + SSL 部署编排
├── docs/                api-contract.md（前后端契约）、schema-reference.md（数据库）
├── tools/               unpack_wpress.py（解包备份）、migrate_wp.py（数据迁移）
├── _migration/          解包的 database.sql、生成的 migrated_data.sql、迁移报告
└── deploy/uploads/      从备份提取的媒体文件（导入服务器 uploads 卷）
```

## 本地开发

```bash
# 后端（需要本地 MySQL 8，application-dev.yml 默认 root/root@localhost:3306，自动建库）
cd blog-backend && mvn spring-boot:run        # :8080，首次启动自动建表+创建 admin/admin123

# 前端
cd blog-frontend && npm install && npm run dev # :3000，/api 反代到 :8080
```

浏览器访问 `http://localhost:3000`，管理后台 `http://localhost:3000/admin`（admin / admin123）。

## 数据迁移（WordPress → 新博客）

1. `_migration/migrated_data.sql` 已由 `tools/migrate_wp.py` 从 `.wpress` 备份生成
   （HTML→Markdown、Gutenberg 块剥离、旧域名去前缀、`/wp-content/uploads/` → `/uploads/`）
2. 后端首次启动建表后执行：
   ```bash
   mysql -u<user> -p blog < _migration/migrated_data.sql
   ```
3. 媒体文件：把 `deploy/uploads/` 内容复制到服务器的 uploads 卷（或本地 `blog-backend/uploads/`）

### 已部署环境升级（2026-09）

背景图/封面已改为**前端静态托管**（`blog-frontend/public/bg/`，随仓库分发，部署即有）：

```bash
git pull && docker compose build web && docker compose up -d
mysql -u<user> -p blog < deploy/sql/2026-09-02-static-bg-covers.sql
```

前台导航栏登录后会显示「✍️ 写文章」入口，写作发布全流程在 `/admin/posts/new`。

注意：管理员密码不迁移（WP `$P$` 哈希不兼容），用默认 admin/admin123 登录后立即修改。

## 生产部署（Docker Compose）

```bash
cd deploy
cp .env.example .env   # 填 DB_PASSWORD / JWT_SECRET / SITE_URL
docker compose up -d --build

# SSL（Let's Encrypt，HTTP 先行验证域名后签发）
docker run --rm -v ./certbot/www:/var/www/certbot certbot/certbot certonly \
  --webroot -w /var/www/certbot -d blog.20260006.xyz --email <you@example.com> --agree-tos
# 证书复制到 ssl/ 下 fullchain.pem + privkey.pem，按 nginx.conf 注释启用 443 段
```

Nginx 路由：`/` → Nuxt SSR(:3000)，`/api` `/uploads` → Spring Boot(:8080)。

## 功能对照（相对原站）

| 功能 | 状态 |
|------|------|
| 文章/页面/说说合一表，保留 WP 原始 ID 与 slug | ✅ |
| 暗色模式（三态）+ 主题色切换 | ✅ |
| 星空粒子背景（点线网络/缓漂/hover聚合/可开关）+ 明暗两套壁纸背景（透明度可调） | ✅ |
| Markdown 渲染（flexmark/markdown-it）+ 代码高亮 + KaTeX | ✅ |
| 嵌套评论 + 验证码 + 审核 | ✅ |
| 说说点赞（IP+UA 去重） | ✅ |
| 全文搜索（ngram，LIKE 兜底） | ✅ |
| 归档时间轴 / 分类标签 / 友链 / Banner | ✅ |
| Live2D 看板娘（Doro 模型，Cubism 4） | ✅ |
| 管理后台（仪表盘 ECharts/编辑器/文件管理/设置） | ✅ |
| sitemap.xml / SEO OG 标签 / 访问统计 | ✅ |
| 密码保护文章 | ✅ |
| PWA / Pangu.js / 图片瀑布流 | ⛔ 裁剪（收益低，后续可加） |

## 验证

- 后端：`mvn test`（11 用例）+ `mvn spring-boot:run` 启动自检
- 前端：`npm run build` 通过；页面渲染与接口联调见最终报告
