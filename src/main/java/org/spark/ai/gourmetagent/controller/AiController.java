package org.spark.ai.gourmetagent.controller;

import org.spark.ai.gourmetagent.agent.GourmetOrchestrator;
import org.spark.ai.gourmetagent.dto.ChatCommand;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin("http://localhost:5173")
public class AiController {
    private final GourmetOrchestrator gourmetOrchestrator;
    public AiController(GourmetOrchestrator gourmetOrchestrator) {
        this.gourmetOrchestrator = gourmetOrchestrator;
    }
    @PostMapping
    public Flux<ServerSentEvent<String>> chat(@RequestBody ChatCommand command,
                                      @RequestHeader(value = "ConversationId", required = false) String conversationId){

        String userMessage = command.message();
        String currentConversationId = (conversationId != null) ? conversationId : UUID.randomUUID().toString();
        return gourmetOrchestrator.chatStream(userMessage, currentConversationId)
                .map(chunk -> ServerSentEvent.builder(chunk)
                        .event("message")
                        .build())
                .concatWithValues(ServerSentEvent.builder("[DONE]").event("done").build());
    }
}
