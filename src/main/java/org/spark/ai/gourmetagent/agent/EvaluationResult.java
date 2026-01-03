package org.spark.ai.gourmetagent.agent;

public record EvaluationResult(
        String reasoning,
        boolean ok,
        String nextSelection
) {
}
