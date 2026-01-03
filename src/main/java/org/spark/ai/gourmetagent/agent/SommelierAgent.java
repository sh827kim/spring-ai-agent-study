package org.spark.ai.gourmetagent.agent;

import org.spark.ai.gourmetagent.tools.SommelierTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;


@Component
public class SommelierAgent {

    private final ChatClient chatClient;
    private final String systemPrompt;
    private final ChatMemory chatMemory;
    private final SommelierTools sommelierTools;

    public SommelierAgent(ChatClient.Builder builder,
                          @Qualifier("workerPrompts") Map<String,String> prompts,
                          ChatMemory chatMemory, SommelierTools sommelierTools) {
        this.chatClient = builder.build();
        this.systemPrompt = prompts.get("sommelier");
        this.chatMemory = chatMemory;
        this.sommelierTools = sommelierTools;
    }
    public String process(String userMessage, String conversationId){
        // 날짜 주입
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd (E요일)", Locale.KOREAN));
        String finalPrompt = new SystemPromptTemplate(systemPrompt)
                .create(Map.of("current_date", currentDate)).getContents();

        //[공식] Agent=ChatClient+Prompt+Tools+Advisor
        return chatClient.prompt()
                .system(finalPrompt)
                .user(userMessage)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(sommelierTools)
                .call()
                .content();
    }
}
