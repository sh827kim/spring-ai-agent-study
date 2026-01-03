package org.spark.ai.gourmetagent.agent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
public class PromptConfig {
    @Value("classpath:prompts/router-system.st")
    private Resource routerResource;

    @Value("classpath:prompts/router-evaluation.st")
    private Resource evaluationResource;

    @Value("classpath:prompts/worker-reservation.st")
    private Resource reservationResource;

    @Value("classpath:prompts/worker-sommelier.st")
    private Resource sommelierResource;

    @Value("classpath:prompts/worker-concierge.st")
    private Resource conciergeResource;

    @Value("classpath:prompts/worker-counselor.st")
    private Resource counselorResource;

    private String loadPrompt(Resource resource) {
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("프롬프트 파일을 읽을 수 없습니다.", e);
        }
    }
    @Bean(name="routerSystemPrompt")
    public String routerSystemPrompt() {
        return loadPrompt(routerResource);
    }
    @Bean(name="routerEvaluatorPrompt")
    public String routerEvaluatorPrompt() {
        return loadPrompt(evaluationResource);
    }

    //   "reservation",  "sommelier", "concierge", "counselor"
    @Bean(name="workerPrompts")
    public Map<String, String> workerPrompts(){
        return Map.of(
                "reservation", loadPrompt(reservationResource),
                "sommelier", loadPrompt(sommelierResource),
                "concierge", loadPrompt(conciergeResource),
                "counselor", loadPrompt(counselorResource)
        );
    }
}
