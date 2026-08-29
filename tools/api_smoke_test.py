#!/usr/bin/env python3
"""End-to-end API smoke test against local backend (port 8080)."""
import json, urllib.request, urllib.parse, sys, io

BASE = "http://localhost:8080"
TOKEN = None
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")

def call(method, path, body=None, headers=None, raw=False):
    req = urllib.request.Request(BASE + path, method=method)
    req.add_header("Content-Type", "application/json")
    if TOKEN and not headers:
        req.add_header("Authorization", "Bearer " + TOKEN)
    for k, v in (headers or {}).items():
        req.add_header(k, v)
    data = json.dumps(body).encode() if body is not None else None
    try:
        with urllib.request.urlopen(req, data) as r:
            payload = r.read()
            if raw:
                return r.status, payload
            return r.status, json.loads(payload)
    except urllib.error.HTTPError as e:
        return e.code, e.read()

ok_count, fail = 0, []

def check(name, cond, detail=""):
    global ok_count
    if cond:
        ok_count += 1
        print(f"  PASS {name}")
    else:
        fail.append(name)
        print(f"  FAIL {name}  {detail}")

print("== 公开接口 ==")
st, r = call("GET", "/api/posts?page=1&size=5")
check("posts list", st == 200 and r["code"] == 0 and r["data"]["total"] > 0, str(r)[:120])
first = r["data"]["records"][0]

st, r = call("GET", "/api/posts?page=1&size=5&categoryId=" + str(first["categories"][0]["id"]) if first.get("categories") else ("GET", "/api/posts"))
check("posts by category", st == 200 and r["code"] == 0)

# 详情：中文 slug 直传 + 数字 id
slug = first["slug"]
path = "/api/posts/" + urllib.parse.quote(slug)
st, r = call("GET", path)
check("post detail by slug", st == 200 and r["code"] == 0 and r["data"]["post"]["id"] == first["id"], str(r)[:150])
check("detail has contentHtml", len(r["data"]["post"].get("contentHtml") or "") > 0)
check("detail has prev/next", "prev" in r["data"] and "next" in r["data"])
pid = first["id"]
st, r = call("GET", f"/api/posts/{pid}")
check("post detail by id", st == 200 and r["code"] == 0)
st, r = call("GET", f"/api/posts/{pid}/related")
check("related posts", st == 200 and r["code"] == 0 and isinstance(r["data"], list))
st, r = call("PUT", f"/api/posts/{pid}/views")
check("incr views", st == 200 and r["code"] == 0 and r["data"]["views"] >= 1)
st2, r2 = call("PUT", f"/api/posts/{pid}/views")
check("views dedup (same ip+ua)", r2["data"]["views"] == r["data"]["views"], f"{r['data']} vs {r2['data']}")

st, r = call("GET", "/api/categories")
check("categories", st == 200 and r["code"] == 0 and len(r["data"]) >= 1)
st, r = call("GET", "/api/tags")
check("tags", st == 200 and r["code"] == 0 and len(r["data"]) >= 1)
st, r = call("GET", "/api/settings/public")
check("public settings siteTitle=Star", r["data"].get("siteTitle") == "Star", str(r)[:100])
st, r = call("GET", "/api/archives")
check("archives", st == 200 and r["code"] == 0 and len(r["data"]) >= 1)
st, raw = call("GET", "/api/sitemap.xml", raw=True)
check("sitemap.xml", st == 200 and b"<urlset" in raw and raw.count(b"<url>") > 10)

st, r = call("GET", "/api/search?q=AI&page=1&size=5")
check("search LIKE", st == 200 and r["code"] == 0 and r["data"]["total"] >= 1, str(r)[:150])

# 评论：验证码流程
st, r = call("GET", "/api/captcha")
check("captcha", st == 200 and r["code"] == 0 and "key" in r["data"])
# 验证码答案拿不到（服务端存内存），这里用错误码 → 期望 400
st, r = call("POST", "/api/comments", {"postId": pid, "author": "tester", "content": "测试评论", "captchaKey": "x", "captchaCode": "0000"})
check("comment with wrong captcha rejected", r["code"] == 400, str(r)[:100])

# 说说
st, r = call("GET", "/api/shuoshuos")
check("shuoshuo list", st == 200 and r["code"] == 0 and isinstance(r["data"]["records"], list))
if r["data"]["records"]:
    sid = r["data"]["records"][0]["id"]
    st, r = call("POST", f"/api/shuoshuos/{sid}/like")
    check("shuoshuo like", st == 200 and r["code"] == 0 and r["data"]["liked"] is True, str(r)[:100])
    st, r = call("GET", f"/api/shuoshuos/{sid}/comments")
    check("shuoshuo comments", st == 200 and r["code"] == 0)

print("== 管理端 ==")
st, r = call("POST", "/api/auth/login", {"username": "admin", "password": "admin123"})
check("login", st == 200 and r["code"] == 0 and r["data"]["token"], str(r)[:100])
TOKEN = r["data"]["token"]
st, r = call("POST", "/api/auth/login", {"username": "admin", "password": "wrong"})
check("bad login rejected", r["code"] != 0)

st, r = call("GET", "/api/admin/posts?page=1&size=10")
check("admin posts", st == 200 and r["code"] == 0 and r["data"]["total"] > 0)
st, r = call("GET", "/api/admin/comments?page=1&size=10")
check("admin comments", st == 200 and r["code"] == 0)
st, r = call("GET", "/api/admin/stats/summary")
check("stats summary", st == 200 and r["code"] == 0 and r["data"]["postCount"] >= 10, str(r)[:120])
st, r = call("GET", "/api/admin/stats/views?days=7")
check("stats views trend", st == 200 and r["code"] == 0 and len(r["data"]) == 7)
st, r = call("GET", "/api/admin/stats/topPosts?limit=5")
check("stats topPosts", st == 200 and r["code"] == 0)

# 未登录访问管理端 → 401
TOKEN = None
st, r = call("GET", "/api/admin/posts")
check("admin requires auth", r["code"] == 401, str(r)[:80])
st, r = call("PUT", f"/api/posts/{pid}/views")  # 公开写接口仍可用
check("public write still ok", r["code"] == 0)

print(f"\n== 结果: {ok_count} PASS, {len(fail)} FAIL ==")
if fail:
    print("failed:", fail)
    sys.exit(1)
