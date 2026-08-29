package com.blog.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/** 评论 HTML 白名单净化（jsoup）。 */
public final class HtmlSanitizer {

    private HtmlSanitizer() {
    }

    private static final Safelist SAFELIST = Safelist.basicWithImages()
            .addTags("code", "pre", "blockquote", "del", "h1", "h2", "h3", "h4", "h5", "h6")
            // 仅允许 http/https 外链；jsoup 对未配置协议的属性天然放行相对路径
            .addProtocols("a", "href", "http", "https")
            .addProtocols("img", "src", "http", "https")
            .addAttributes("img", "alt", "width", "height");

    /** 清理后保留白名单标签，去除脚本/事件属性。 */
    public static String clean(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return Jsoup.clean(html, "", SAFELIST);
    }
}
