package com.example.jevclassifier.controller;

import com.example.jevclassifier.dto.ClassificationRequest;
import com.example.jevclassifier.dto.ClassificationItem;
import com.example.jevclassifier.service.ClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.BiConsumer;

@RestController
@RequestMapping("/classification")
@RequiredArgsConstructor
public class ClassificationController {

    private final ClassificationService classificationService;
    @Qualifier("classificationExecutor")
    private final ThreadPoolTaskExecutor classificationExecutor;

    @PostMapping(value = "/llm", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter classifyLLM(@RequestBody ClassificationRequest req) {
        return stream(req, (onResult, onError) -> classificationService.streamLLM(req, onResult, onError));
    }

    @PostMapping(value = "/jev", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter classifyJev(@RequestBody ClassificationRequest req) {
        return stream(req, (onResult, onError) -> classificationService.streamJev(req, onResult, onError));
    }

    private SseEmitter stream(ClassificationRequest req, ClassificationStream operation) {
        if (req == null || req.getContexts() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contexts is required");
        }

        SseEmitter emitter = new SseEmitter(0L);
        classificationExecutor.execute(() -> {
            try {
                operation.run(
                        (index, item) -> send(emitter, "result", new ResultEvent(index, item)),
                        (index, error) -> send(emitter, "error", new ErrorEvent(index, "Classification failed"))
                );
                emitter.complete();
            } catch (UncheckedIOException error) {
                emitter.completeWithError(error);
            } catch (RuntimeException error) {
                try {
                    send(emitter, "error", new ErrorEvent(-1, "Classification failed"));
                    emitter.complete();
                } catch (UncheckedIOException sendError) {
                    emitter.completeWithError(sendError);
                }
            }
        });
        return emitter;
    }

    private void send(SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    @FunctionalInterface
    private interface ClassificationStream {
        void run(BiConsumer<Integer, ClassificationItem> onResult,
                 BiConsumer<Integer, RuntimeException> onError);
    }

    public record ResultEvent(int index, ClassificationItem item) {}

    public record ErrorEvent(int index, String message) {}
}
