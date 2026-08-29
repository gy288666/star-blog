package com.blog.util;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.List;

/** Markdown 渲染工具（flexmark）。html2md 留给迁移脚本使用。 */
public final class MdConverter {

    private MdConverter() {
    }

    private static final Parser PARSER;
    private static final FlexmarkHtmlConverter HTML2MD;

    static {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                TaskListExtension.create(),
                AutolinkExtension.create()));
        PARSER = Parser.builder(options).build();
        HTML2MD = FlexmarkHtmlConverter.builder(options).build();
    }

    /** Markdown -> HTML，入参为空返回空串。 */
    public static String toHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        Node document = PARSER.parse(markdown);
        return com.vladsch.flexmark.html.HtmlRenderer.builder(PARSER.getOptions())
                .build().render(document);
    }

    /** HTML -> Markdown（迁移用）。 */
    public static String toMarkdown(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return HTML2MD.convert(html);
    }
}
