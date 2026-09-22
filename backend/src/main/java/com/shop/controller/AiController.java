package com.shop.controller;

import com.shop.common.Result;
import com.shop.service.AiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** AI 能力接口：助手对话（支持多轮上下文） / 商品描述生成 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * AI 助手对话。请求体：
     *   message  : 本次问题（必填）
     *   history  : 最近对话 [{role:"user"|"assistant", content:"..."}]，可选，用于多轮上下文
     */
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, Object> body) {
        Object messageObj = body.get("message");
        String message = messageObj == null ? "" : String.valueOf(messageObj).trim();
        if (message.isEmpty()) {
            return Result.error("请输入消息内容");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) body.get("history");
        return Result.success(aiService.chat(message, history));
    }

    /** 生成商品描述 */
    @PostMapping("/describe")
    public Result<String> describe(@RequestBody Map<String, String> body) {
        return Result.success(aiService.describeProduct(
                body.getOrDefault("title", ""),
                body.getOrDefault("category", ""),
                body.getOrDefault("price", ""),
                body.getOrDefault("sellPoints", "")));
    }
}