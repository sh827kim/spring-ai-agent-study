package org.spark.ai.gourmetagent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Component
public class CounselorAgent {
    private final ChatClient chatClient;
    private final String systemPrompt;
    private final ChatMemory chatMemory;

    public CounselorAgent(ChatClient.Builder builder,
                          @Qualifier("workerPrompts") Map<String,String> prompts,
                          ChatMemory chatMemory) {
        this.chatClient = builder.build();
        this.systemPrompt = prompts.get("counselor"); // {current_date}
        this.chatMemory = chatMemory;
    }

    public Flux<String> streamFinal(String userMessage,
                                    String conversationId,
                                    WorkerResult lastWorkerResult) {

        String currentDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd (E요일)", Locale.KOREAN));

        String finalPrompt = new SystemPromptTemplate(systemPrompt)
                .create(Map.of("current_date", currentDate))
                .getContents();

        // counselor는 “최종 응답 생성”만. 보통 tools는 붙이지 않는 게 안정적.
        return chatClient.prompt()
                .system(finalPrompt)
                .user("""
                      [USER_MESSAGE]
                      %s

                      [LAST_WORKER_TYPE]
                      %s

                      [LAST_WORKER_OUTPUT]
                      %s
                      """.formatted(userMessage, lastWorkerResult.workerType(), lastWorkerResult.content()))
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
}
