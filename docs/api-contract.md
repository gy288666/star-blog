# API 契约（前后端唯一依据）

Base URL: `/api`。所有响应统一包裹：

```json
{ "code": 0, "msg": "ok", "data": <payload> }
```

- `code=0` 成功；非 0 失败（401 未认证 / 403 无权限 / 400 参数错误 / 500 服务器错误）。
- HTTP 状态码始终 200（错误信息在 body 中），前端根据 code 判断。
- 分页统一返回：

```json
{ "records": [...], "total": 123, "page": 1, "size": 10 }
```

- 认证：`Authorization: Bearer <jwt>`。登录返回 `{ "token": "...", "user": { "id", "username", "nickname", "avatar", "role" } }`。

## Post 实体字段（前端可见 VO）

```
id, type(0文章/1页面/2说说), title, slug, summary, cover, status(0草稿/1发布/2密码保护),
password(仅管理端返回), authorId, authorNickname, views, upvotes, isTop, allowComment,
commentCount, publishedAt, createTime, updateTime,
contentMd, contentHtml（详情接口返回；列表接口不返回正文）,
categories: [{id,name,slug}], tags: [{id,name,slug}]
```

## 分类/标签

```
Category: { id, name, slug, description, parentId, sortOrder, postCount }
Tag:      { id, name, slug, postCount }
```

## Comment

```
{ id, postId, parentId, rootId, author, email, website, avatar, contentMd, contentHtml,
  isAdmin, createTime, children: [...] }
```
- 公开列表只返回已审核(status=1)的。`children` 只到二级（rootId 分组下按 parentId 归到根下展示 `@某某` 由前端处理）。
- 管理端列表返回扁平结构含 `status`、`ip`、`userAgent`、`postTitle`。

## 公开接口

| 方法 | 路径 | 参数/请求体 | 返回 |
|------|------|-------------|------|
| GET | /api/posts | `page,size,categoryId,tagId,keyword`(keyword 仅后台用) | 分页 Post VO（按 isTop desc, publishedAt desc） |
| GET | /api/posts/{idOrSlug} | id 或 slug | Post VO 详情 + prev{id,title,slug} + next{id,title,slug} |
| GET | /api/posts/{id}/related | - | `Post VO[]`（同分类/标签，最多 6 篇，不含正文） |
| PUT | /api/posts/{id}/views | -（同一 IP+UA 24h 去重） | `{views}` |
| GET | /api/pages/{slug} | slug | 页面 VO（type=1） |
| GET | /api/categories | - | `Category[]` |
| GET | /api/tags | - | `Tag[]` |
| GET | /api/comments | `postId,page,size`（默认 size=10） | 分页 Comment（rootId=0 的根评论分页，children 内嵌） |
| POST | /api/comments | `{postId,parentId,rootId,author,email,website,content,captchaKey,captchaCode}` | Comment VO |
| GET | /api/shuoshuos | `page,size` | 分页 Shuoshuo VO（含 contentHtml、upvotes、commentCount） |
| POST | /api/shuoshuos/{id}/like | -（IP+UA 去重） | `{upvotes, liked:true}` |
| GET | /api/shuoshuos/{id}/comments | `page,size` | 分页 Comment（该说说下的评论） |
| GET | /api/search | `q,page,size,type(article,page,shuoshuo 逗号分隔,默认 article)` | 分页 Post VO（title/正文命中，不含正文，返回 hitTitle? 无需） |
| GET | /api/banners | - | `Banner[]` 活跃横幅 |
| GET | /api/friends | - | `Friend[]`（status=1，按 sortOrder） |
| GET | /api/settings/public | - | `{siteTitle, siteSubtitle, siteLogo, footerText, icpText, bannerTitle, bannerSubtitle, bannerImage, bannerTypingEffect, allowComment, darkModeDefault}` |
| GET | /api/auth/login?NO | POST /api/auth/login `{username,password}` | `{token,user}` |
| GET | /api/captcha | - | `{key, image}`（image 为 dataURL png） |
| GET | /api/visit | NO | POST /api/visit `{url, referer}` 记录访问日志，返回 `{code:0}` |
| GET | /api/archives | - | `[{ "year": "2026", "posts": [{id,title,slug,createTime}] }]` 按年分组 |
| GET | /api/sitemap.xml | - | XML 文本（Content-Type: application/xml），含文章/页面/说说/标签/分类 URL |

## 管理接口（均需 Bearer Token，前缀 /api/admin）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/admin/posts | 新建 `{type,title,slug,summary,cover,contentMd,status,password,isTop,allowComment,categoryIds[],tagIds[],publishedAt}`，contentHtml 后端渲染 |
| PUT | /api/admin/posts/{id} | 更新（同上） |
| DELETE | /api/admin/posts/{id} | 删除（连带关联表） |
| GET | /api/admin/posts | `page,size,type,status,keyword` 分页（含草稿、含正文 md） |
| GET | /api/admin/posts/{id} | 详情（编辑用） |
| GET/POST/PUT/DELETE | /api/admin/categories(/{id}) | 分类 CRUD |
| GET/POST/PUT/DELETE | /api/admin/tags(/{id}) | 标签 CRUD |
| GET | /api/admin/comments | `page,size,status,postId` 扁平分页 |
| PUT | /api/admin/comments/{id}/status | `{status: 0|1|2}` 审核 |
| DELETE | /api/admin/comments/{id} | 删除（连带子评论） |
| POST | /api/admin/comments | 管理员回复 `{postId,parentId,rootId,content}`（is_admin=1，免验证码直接通过） |
| GET/POST/PUT/DELETE | /api/admin/friends(/{id}) | 友链 CRUD |
| GET/POST/PUT/DELETE | /api/admin/banners(/{id}) | 横幅 CRUD |
| GET | /api/admin/settings | 全部设置 `{key:value}` |
| PUT | /api/admin/settings | `{key:value,...}` 批量保存 |
| POST | /api/admin/files/upload | multipart `file`，返回 `{url:"/uploads/xxx.png", name}` |
| GET | /api/admin/files | `dir,page,size` 列出上传目录文件 `{name,url,size,modifyTime}` |
| DELETE | /api/admin/files | `?path=` 删除 |
| GET | /api/admin/stats/summary | `{postCount,shuoshuoCount,commentCount,pendingComments,viewsSum,friendCount,attachCount,runDays}` |
| GET | /api/admin/stats/views?days=7 | `[{date:"2026-08-28", count}]` 访问趋势 |
| GET | /api/admin/stats/topPosts?limit=10 | `[{id,title,views}]` |
| PUT | /api/admin/password | `{oldPassword,newPassword}` 修改当前用户密码 |
| PUT | /api/admin/profile | `{nickname,avatar,email}` |

## 错误码

- 0 成功；400 参数/业务错误；401 未登录或 token 过期；403 无权限；500 服务器错误。

## 设置键（blog_setting，前后端共用）

siteTitle, siteSubtitle, siteLogo, footerText, icpText, bannerTitle, bannerSubtitle,
bannerImage, bannerTypingEffect(0/1), allowComment(0/1), darkModeDefault(0/1/auto),
siteUrl, notifyEmail
