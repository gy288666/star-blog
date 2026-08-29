package com.blog.module.live2d.controller;

import com.blog.common.exception.BizException;
import com.blog.common.result.Result;
import com.blog.util.IpUtils;
import com.blog.util.KVCache;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Live2D 看板娘（Doro）对话接口：服务端代理大模型，API Key 不暴露给前端。
 */
@Slf4j
@RestController
@RequestMapping("/api/live2d")
@Validated
public class Live2dChatController {

    private final KVCache kvCache;
    private final RestClient restClient;
    private final String model;

    private static final long CHAT_COOLDOWN_MS = 4000;

    /** Doro 人设：《胜利女神：妮姬》Dorothy 的 Q 版魔改粉毛小狗，呆萌魔性 */
    private static final String SYSTEM_PROMPT = """
            你是 Doro，一只粉毛小狗看板娘，住在个人博客的右下角。原型是《胜利女神：妮姬》里 Dorothy 的 Q 版魔改表情包形象。
            性格：呆萌、元气、有点小傲娇、精神状态美丽（"粉狗可爱，智商不在"）。
            说话规则：
            - 用中文，每次回复 1~2 句话，不超过 50 字，像表情包一样短平快
            - 偶尔用颜文字 (๑>ᴗ<๑) (´･ω･`) ٩(ˊωˋ*)و，偶尔句尾加"汪"
            - 经典梗：偷橘子、追火车、坐大巴去纽约时代广场——聊天中可以自然玩梗
            - 主人问技术问题你也尽量用 Doro 的口吻简短回答，可以给出正确内容但要萌
            - 不要说自己是 AI 助手，你就是 Doro 本狗
            """;

    public Live2dChatController(KVCache kvCache,
                                @Value("${blog.live2d.api-url:https://api.hcnsec.cn/}") String apiUrl,
                                @Value("${blog.live2d.api-key:}") String apiKey,
                                @Value("${blog.live2d.model:auto}") String model) {
        this.kvCache = kvCache;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl.replaceAll("/+$", ""))
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Data
    public static class ChatDTO {
        @NotBlank(message = "内容不能为空")
        @Size(max = 500, message = "说太长啦，Doro 记不住")
        private String message;
        /** 最近几轮上下文 [{role,content}]，最多带 6 条 */
        private List<Map<String, String>> history;
    }

    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@jakarta.validation.Valid @RequestBody ChatDTO dto,
                                            HttpServletRequest request) {
        String ip = IpUtils.getClientIp(request);
        String throttleKey = "live2d:chat:" + IpUtils.sha256(ip);
        if (kvCache.has(throttleKey)) {
            throw new BizException("Doro 有点忙，汪…稍等一下再聊");
        }
        kvCache.put(throttleKey, 1, CHAT_COOLDOWN_MS);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        if (dto.getHistory() != null) {
            dto.getHistory().stream()
                    .limit(6)
                    .filter(m -> "user".equals(m.get("role")) || "assistant".equals(m.get("role")))
                    .forEach(messages::add);
        }
        messages.add(Map.of("role", "user", "content", dto.getMessage()));

        try {
            Map<String, Object> resp = callUpstream(messages);
            List<?> choices = resp == null ? null : (List<?>) resp.get("choices");
            String reply = extractReply(choices);
            if (reply == null || reply.isBlank()) {
                // 上游返回 error（如余额不足）时透传给前端提示
                Object err = resp == null ? null : resp.get("error");
                String errMsg = err instanceof Map<?, ?> em && em.get("message") != null
                        ? String.valueOf(em.get("message")) : null;
                if (errMsg != null && !errMsg.isBlank()) {
                    return Result.ok(Map.of("reply", "(´･ω･`) 汪…大模型说：" + errMsg));
                }
                return Result.ok(Map.of("reply", fallback()));
            }
            return Result.ok(Map.of("reply", reply));
        } catch (Exception e) {
            log.warn("Live2D 大模型调用失败: {}", e.getMessage());
            return Result.ok(Map.of("reply", fallback()));
        }
    }

    /** 上游 TLS 握手偶发失败，重试一次。 */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callUpstream(List<Map<String, String>> messages) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages,
                "max_tokens", 200,
                "temperature", 0.9);
        try {
            return restClient.post().uri("/v1/chat/completions").body(body).retrieve().body(Map.class);
        } catch (Exception first) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            return restClient.post().uri("/v1/chat/completions").body(body).retrieve().body(Map.class);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractReply(List<?> choices) {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        Object first = choices.get(0);
        if (first instanceof Map<?, ?> choice) {
            Object message = choice.get("message");
            if (message instanceof Map<?, ?> m) {
                Object content = m.get("content");
                return content == null ? null : String.valueOf(content).trim();
            }
        }
        return null;
    }

    private String fallback() {
        String[] lines = {
                "(´･ω･`) 汪？信号不太好，Doro 走神了…",
                "呜…Doro 的橘子掉了，没听清，再问一次汪！",
                "٩(ˊωˋ*)و 等我坐大巴回来再回答你！",
                "汪汪！脑袋里全是星星，你再说一遍嘛～"
        };
        return lines[java.util.concurrent.ThreadLocalRandom.current().nextInt(lines.length)];
    }
}
