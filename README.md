# Star Blog — Spring Boot 3 + Nuxt 3 个人博客系统

原 WordPress（Argon 主题）个人博客的完整重构。

**功能一览**：SSR 博客 + 管理后台 / 星空粒子背景 + 明暗两套壁纸 / Live2D 看板娘（可拖拽、点击冒气泡、接入大模型角色扮演对话）/ Markdown + KaTeX + 代码高亮 / 嵌套评论 + 验证码 / 说说点赞 / 全文搜索 / 归档时间轴 / 友链 / 密码保护文章 / sitemap / ECharts 仪表盘 / WordPress 一键迁移工具链

> **访客互动**：任何人都能发说说（浏览器身份识别，仅发布人与管理员可删）；文章封面每周自动换新且全局不重复；关于页自带留言区与官方 QQ 群入口。

![技术栈](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F) ![Nuxt](https://img.shields.io/badge/Nuxt-3.x-00DC82) ![MySQL](https://img.shields.io/badge/MySQL-8-4479A1) ![Java](https://img.shields.io/badge/Java-21-007396)

## 项目结构

```
├── blog-backend/        后端：Spring Boot 3.3 + Java 21 + MyBatis-Plus + Flyway（REST API :8080）
├── blog-frontend/       前端：Nuxt 3 SSR 博客 + /admin 管理后台 SPA（Element Plus）
├── deploy/              部署编排：Docker Compose + Nginx + SQL 升级脚本
├── docs/                api-contract.md（前后端契约）、schema-reference.md（数据库）
└── tools/               unpack_wpress.py（解包备份）、migrate_wp.py（数据迁移）、maven-settings.xml
```

## 本地开发

```bash
# 后端：需要本地 MySQL 8（默认 root/root@localhost:3306，自动建库）
cd blog-backend && mvn spring-boot:run     # :8080，首次启动自动建表 + 创建 admin/admin123

# 前端
cd blog-frontend && npm install && npm run dev   # :3000，/api 自动代理到 :8080
```

浏览器访问 `http://localhost:3000`，管理后台 `/admin`（admin / admin123，登录后导航栏会出现「写文章」按钮）。

**看板娘对话**需配置大模型（环境变量 `LIVE2D_API_URL` / `LIVE2D_API_KEY` / `LIVE2D_MODEL`），任何 OpenAI 兼容接口均可，key 只存后端不暴露给浏览器。

**文章封面**默认每周一 03:00 从哲风壁纸（haowallpaper.com）随机刷新一轮；管理后台仪表盘有「随机更换文章封面」一键按钮（也可调 `POST /api/admin/covers/refresh`）。不需要时设环境变量 `COVER_REFRESH_ENABLED=false` 关闭。

封面去重保证：每次刷新排除**全部历史用过的壁纸**（`blog_cover_history` 表）与当前在用的封面，一篇文章之间、相邻几轮之间都不会出现重复图；候选里混有动态壁纸视频时会自动跳过，好图不足时自动重置历史重试一轮（重置后仍排除当前封面）。壁纸图片会下载到本站 `uploads/covers/` 存储展示，不受壁纸站防盗链影响。背景壁纸（明暗两套）内置在前端 `blog-frontend/public/bg/`，部署即有。

**说说**：任意访客可在说说页发布（昵称可留空，浏览器身份识别，同 IP 60 秒限流），仅发布人本人与管理员可删除。

---

# 服务器部署指南（零基础版）

## 准备工作

| 需要什么 | 说明 | 大致花费 |
|---------|------|---------|
| 一台云服务器 | 阿里云/腾讯云轻量服务器即可，**2核4G 起步**（1核2G 跑不动 Docker 全家桶），系统选 **Ubuntu 22.04** | 约 50-100 元/月 |
| 一个域名 | 已备案（国内服务器必须），如 `20260006.xyz` | 几十元/年 |
| 本机能 SSH | Windows 10/11 自带 `ssh` 命令，Mac/Linux 终端同理 | 免费 |

### 1. 域名解析

到域名控制台添加一条 **A 记录**：主机记录填 `blog`（或 `@`），记录值填服务器的**公网 IP**。等 5-10 分钟生效，`ping blog.你的域名` 能看到服务器 IP 即可。

### 2. 首次登录服务器

```bash
ssh root@服务器公网IP      # 密码在云控制台重置
```

先做一次系统更新：

```bash
apt update && apt upgrade -y
```

---

## 方式一：Docker Compose 部署（推荐，全自动）

> 优点：一条命令拉起 MySQL + 后端 + 前端 + Nginx，环境隔离、升级方便。

### 第 1 步：安装 Docker

```bash
# 国内服务器用镜像源安装（一行搞定）
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun

# 启动并验证
systemctl enable --now docker
docker --version        # 显示版本号即成功
docker compose version  # v2 插件，部署要用
```

### 第 2 步：拉代码

```bash
apt install -y git
cd /opt
git clone https://github.com/gy288666/star-blog.git
cd star-blog
```

### 第 3 步：配置密钥

```bash
cd deploy
cp .env.example .env
nano .env    # nano 里改完按 Ctrl+O 保存、Ctrl+X 退出
```

```ini
DB_PASSWORD=你的数据库密码          # 自己编一个强密码
JWT_SECRET=一段至少32位的随机字符串  # 自己编，用来签发登录令牌
SITE_URL=https://blog.你的域名
```

> 看板娘对话功能再追加一行 `LIVE2D_API_KEY=你的大模型key`（不用看板娘可跳过）。

### 第 4 步：启动

```bash
cd /opt/star-blog/deploy
docker compose up -d --build     # 首次构建约 10-20 分钟，喝杯茶

docker compose ps                # 四个容器都应是 Up/running
curl http://localhost            # 返回 HTML 即成功
```

此时浏览器访问 `http://blog.你的域名` 应该已经能看到博客了（只是 http 不带锁标）。

### 第 5 步：配置 HTTPS（免费 SSL 证书）

```bash
apt install -y certbot
cd /opt/star-blog/deploy && docker compose restart nginx

# 签发证书（要求：域名已解析到本机、80 端口可访问）
certbot certonly --webroot -w /opt/star-blog/deploy/certbot/www \
  -d blog.你的域名 --email 你的邮箱 --agree-tos

# 证书装进 nginx
mkdir -p ssl
cp /etc/letsencrypt/live/blog.你的域名/fullchain.pem ssl/
cp /etc/letsencrypt/live/blog.你的域名/privkey.pem ssl/
```

然后编辑 `deploy/nginx.conf`：**删掉** `# return 301 ...` 前面的 `#`（开启 http 跳转 https），把 `# server { listen 443 ...` 那一段的注释全部打开，再重启：

```bash
nano /opt/star-blog/deploy/nginx.conf
docker compose restart nginx
```

完成 ✅ 访问 `https://blog.你的域名`，管理后台 `/admin`，账号 `admin / admin123`（**立即去 站点设置 → 修改密码**）。

### 日常运维速查

```bash
docker compose logs -f backend   # 看后端日志（Ctrl+C 退出）
docker compose restart backend   # 重启某个服务
docker compose up -d --build     # 更新代码后重新部署（git pull 之后执行）
docker compose down              # 停止全部（数据在 mysql_data 卷里，不会丢）
docker stats                     # 观察内存/CPU 占用
```

---

## 方式二：宝塔面板部署（适合不想敲命令）

> 优点：图形界面，点鼠标完成。

1. 安装宝塔：`wget -O install.sh https://download.bt.cn/install/install_lts.sh && bash install.sh`，记下面板地址和密码
2. 面板 → 软件商店 → 安装 **MySQL 8.0**、**Nginx**、**Java 项目管理器**（选 JDK21）、**Node.js 版本管理器**（装 Node 22）
3. 面板 → 数据库 → 新建 `blog` 库（utf8mb4）
4. `/opt/star-blog` 拉代码（同方式一第 2 步）
5. **后端**：先在本机（或服务器装 Maven 后）执行 `cd blog-backend && mvn -DskipTests package -s ../tools/maven-settings.xml` 生成 `target/blog-backend-1.0.0.jar`；Java 项目管理器 → 添加项目，JDK 选 21，端口 8080，环境变量：
   ```
   SPRING_PROFILES_ACTIVE=prod
   DB_HOST=127.0.0.1  DB_NAME=blog  DB_USER=root  DB_PASSWORD=你的密码
   JWT_SECRET=一段随机字符串  UPLOAD_DIR=/opt/star-blog/uploads  SITE_URL=https://blog.你的域名
   ```
6. **前端**：
   ```bash
   cd blog-frontend
   npm install --registry=https://registry.npmmirror.com
   NUXT_API_ORIGIN=http://127.0.0.1:8080 npm run build
   npm i -g pm2
   NUXT_API_ORIGIN=http://127.0.0.1:8080 pm2 start "node .output/server/index.mjs" --name blog-web
   pm2 save && pm2 startup     # 开机自启
   ```
7. **Nginx**：面板 → 网站 → 添加站点（域名 `blog.你的域名`）→ 配置文件里把 `deploy/blog_proxy.inc` 的内容粘贴进 `server {}`，并把其中 `http://backend:8080` 全部改成 `http://127.0.0.1:8080`、`http://web:3000` 改成 `http://127.0.0.1:3000`
8. SSL：面板 → 网站 → SSL → Let's Encrypt 一键申请，开强制 HTTPS

---

## 方式三：裸机手动部署（进阶）

```bash
# 依赖：JDK 21 + Maven + Node 22 + MySQL 8 + Nginx（apt install，版本要够）
# 后端
cd blog-backend
mvn -DskipTests package -s ../tools/maven-settings.xml
nohup java -jar -Xms256m -Xmx768m target/blog-backend-1.0.0.jar \
  --spring.profiles.active=prod > blog.log 2>&1 &

# 前端
cd blog-frontend
npm install --registry=https://registry.npmmirror.com
npm run build
NUXT_API_ORIGIN=http://127.0.0.1:8080 pm2 start "node .output/server/index.mjs" --name blog-web
pm2 save && pm2 startup     # 开机自启

# Nginx：参考 deploy/nginx.conf + deploy/blog_proxy.inc，proxy_pass 指向 127.0.0.1
```

---

## 把旧 WordPress 的数据搬过来（可选）

如果之前是 WordPress 博客，后台装 **All-in-One WP Migration** 插件导出 `.wpress` 备份，然后：

```bash
# 1. 解包备份（得到 database.sql + 全部媒体文件），需要本机有 Python 3
pip install markdownify
python tools/unpack_wpress.py 备份文件.wpress _migration/backup database.sql

# 2. 生成新库数据（HTML→Markdown、URL 重写、封面/背景设置自动映射）
python tools/migrate_wp.py            # 产物：_migration/migrated_data.sql

# 3. 后端首次启动建表后导入
mysql -u<user> -p blog < _migration/migrated_data.sql
```

- 管理员密码不迁移（WP `$P$` 哈希与 BCrypt 不兼容），用默认 `admin/admin123` 登录后立即修改
- 背景图/封面已随仓库分发（`blog-frontend/public/bg/`），部署即有，无需手动传图

## 已部署环境的版本升级

```bash
cd /opt/star-blog
git pull
cd deploy && docker compose up -d --build
# 若 deploy/sql/ 下有新的升级脚本，按日期顺序执行：
mysql -u<user> -p blog < deploy/sql/2026-09-02-static-bg-covers.sql
```

## 常见问题

| 现象 | 原因与解决 |
|------|-----------|
| 访问 502 | 后端没起来：`docker compose logs backend` 看日志；本机 `curl localhost:8080/api/posts` 验证 |
| 页面能开但没数据 | 数据库连不上：检查 `.env` 的 DB_PASSWORD；改过密码需 `docker compose down -v` 重建（**会清数据**） |
| 80/443 端口不通 | 云控制台**安全组**放行 80、443、22；服务器防火墙 `ufw allow 80,443/tcp` |
| 构建时 npm/maven 超时 | 国内网络，前端 Dockerfile 已配 npmmirror；Maven 用仓库里的阿里云镜像配置 `tools/maven-settings.xml` |
| 内存不足容器被杀 | 2G 机器升级到 4G；或给 MySQL 限内存；`docker stats` 观察占用 |
| 证书 3 个月后过期 | 加自动续期 crontab：`0 3 * * * certbot renew --quiet`（用宝塔则自动处理） |

## 与原方案的两处取舍

- **未引入 Redis**：个人博客单实例，验证码/浏览量去重用进程内 TTL 缓存实现（`KVCache`），少维护一个中间件
- **PWA / Pangu.js / 瀑布流**裁剪：收益低，需要时再加

## 验证

- 后端：`mvn test`（11 用例）+ `tools/api_smoke_test.py`（29 项接口冒烟）
- 前端：`npm run build` 零错误；写作→发布→展示、评论、说说游客发布/删除、封面随机不重复、看板娘对话均已实测
