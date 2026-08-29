package com.blog.common.constant;

/** 全局常量：缓存 key 前缀与 TTL、类型枚举值。 */
public final class Constants {

    private Constants() {
    }

    /** post.type */
    public static final int TYPE_ARTICLE = 0;
    public static final int TYPE_PAGE = 1;
    public static final int TYPE_SHUOSHUO = 2;

    /** post.status */
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_PASSWORD = 2;

    /** comment.status */
    public static final int COMMENT_PENDING = 0;
    public static final int COMMENT_APPROVED = 1;
    public static final int COMMENT_SPAM = 2;

    /** 浏览量去重：24h */
    public static final long TTL_VIEW_DEDUP = 24 * 3600 * 1000L;
    /** 验证码：5 分钟 */
    public static final long TTL_CAPTCHA = 5 * 60 * 1000L;

    public static final String CACHE_VIEW = "post:view:";
    public static final String CACHE_CAPTCHA = "captcha:";

    /** 设置键默认值（库中不存在时兜底） */
    public static final String DEFAULT_SITE_URL = "https://blog.20260006.xyz";
}
