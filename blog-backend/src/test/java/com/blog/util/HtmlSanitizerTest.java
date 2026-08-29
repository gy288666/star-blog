package com.blog.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 评论 HTML 白名单净化测试。 */
class HtmlSanitizerTest {

    @Test
    void stripsScriptAndEventHandlers() {
        String out = HtmlSanitizer.clean("<p onclick=\"alert(1)\">hi<script>alert(2)</script></p>");
        assertFalse(out.toLowerCase().contains("script"), out);
        assertFalse(out.toLowerCase().contains("onclick"), out);
        assertTrue(out.contains("hi"), out);
    }

    @Test
    void keepsBasicFormatting() {
        String out = HtmlSanitizer.clean("<p><strong>粗</strong><em>斜</em><code>码</code></p>");
        assertTrue(out.contains("<strong>"), out);
        assertTrue(out.contains("<em>"), out);
        assertTrue(out.contains("<code>"), out);
    }

    @Test
    void javascriptHrefRemoved() {
        String out = HtmlSanitizer.clean("<a href=\"javascript:alert(1)\">x</a>");
        assertFalse(out.toLowerCase().contains("javascript:"), out);
    }

    @Test
    void nullAndBlankSafe() {
        assertEquals("", HtmlSanitizer.clean(null));
        assertEquals("", HtmlSanitizer.clean("  "));
    }
}
