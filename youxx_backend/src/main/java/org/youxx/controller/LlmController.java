package org.youxx.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.youxx.common.result.Result;
import org.youxx.service.LlmService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmController {

    private final LlmService llmService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 普通对话接口（非流式）
     */
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, Object> body) {
        String userMessage = (String) body.get("message");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) body.get("history");

        try {
            String response = llmService.chat(userMessage, history);
            return Result.success(response);
        } catch (Exception e) {
            log.error("LLM对话失败", e);
            return Result.error("AI助手暂时不可用，请稍后再试");
        }
    }

    /**
     * SSE流式对话接口
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> body) {
        String userMessage = (String) body.get("message");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) body.get("history");

        SseEmitter emitter = new SseEmitter(120000L);

        executor.execute(() -> {
            try {
                llmService.chatStream(userMessage, history, new LlmService.LlmStreamCallback() {
                    @Override
                    public void onToken(String token) {
                        try {
                            emitter.send(SseEmitter.event().name("token").data(token));
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onComplete(String fullResponse) {
                        try {
                            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        try {
                            emitter.send(SseEmitter.event().name("error").data(error.getMessage()));
                        } catch (IOException e) {
                            // ignore
                        }
                        emitter.completeWithError(error);
                    }
                });
            } catch (Exception e) {
                log.error("流式对话异常", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("AI助手暂时不可用"));
                } catch (IOException ex) {
                    // ignore
                }
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });

        emitter.onError(ex -> {
            log.warn("SSE连接异常", ex);
        });

        return emitter;
    }
}
