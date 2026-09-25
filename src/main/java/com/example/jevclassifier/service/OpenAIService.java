package com.example.jevclassifier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final OpenAiChatModel openAiChatModel;

    public String generate(String text,String sysPrompt) {

        // 메시지
        SystemMessage systemMessage = new SystemMessage(sysPrompt);
        UserMessage userMessage = new UserMessage(text);

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-5.6-luna")//
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), options);

        // 요청 및 응답
        ChatResponse response = openAiChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}