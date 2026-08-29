package com.blog.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Markdown 渲染与 HTML→MD 转换测试。 */
class MdConverterTest {

    @Test
    void toHtmlRendersHeadingsAndBold() {
        String html = MdConverter.toHtml("# 标题\n\n**加粗**文字");
        assertTrue(html.contains("<h1>标题</h1>"), html);
        assertTrue(html.contains("<strong>加粗</strong>"), html);
    }

    @Test
    void toHtmlRendersTableAndCode() {
        String html = MdConverter.toHtml("| a | b |\n|---|---|\n| 1 | 2 |\n\n```java\nint x = 1;\n```");
        assertTrue(html.contains("<table>"), html);
        assertTrue(html.contains("<code"), html);
    }

    @Test
    void emptyInputReturnsEmpty() {
        assertEquals("", MdConverter.toHtml(null));
        assertEquals("", MdConverter.toHtml("  "));
        assertEquals("", MdConverter.toMarkdown(null));
    }

    @Test
    void toMarkdownRoundTrip() {
        String html = "<h2>二级标题</h2><p>段落 <strong>加粗</strong></p><ul><li>项</li></ul>";
        String md = MdConverter.toMarkdown(html);
        assertTrue(md.contains("二级标题"), md);
        assertTrue(md.contains("加粗"), md);
        assertTrue(md.contains("项"), md);
    }
}
