package org.spark.ai.gourmetagent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class IntentRouter {
    private final ChatClient chatClient;
    private final String routerPrompt;
    private final String evaluatorPrompt;

    public IntentRouter(ChatClient.Builder builder,
                        @Qualifier("routerSystemPrompt") String routerPrompt,
                        @Qualifier("routerEvaluatorPrompt") String evaluatorPrompt) {
        this.chatClient = builder.build();
        this.routerPrompt = routerPrompt;
        this.evaluatorPrompt = evaluatorPrompt;
    }


    public RoutingDecision determineWorker(String userMessage) {
        RoutingDecision response = chatClient.prompt()
                .system(routerPrompt)
                .user(userMessage)
                .call()
                .entity(RoutingDecision.class);

        System.out.println("라우터 분석:" + response.selection() + "(" + response.reasoning()+")");

        return response;
    }

    public EvaluationResult evaluate(String userMessage, WorkerResult workerResult) {
        EvaluationResult eval = chatClient.prompt()
                .system(evaluatorPrompt)
                .user("""
                      [USER_MESSAGE]
                      %s

                      [WORKER_TYPE]
                      %s

                      [WORKER_OUTPUT]
                      %s
                      """.formatted(userMessage, workerResult.workerType(), workerResult.content()))
                .call()
                .entity(EvaluationResult.class);

        System.out.println("라우터 평가: ok=" + eval.ok() + ", next=" + eval.nextSelection()
                + " (" + eval.reasoning() + ")");
        return eval;
    }
}
