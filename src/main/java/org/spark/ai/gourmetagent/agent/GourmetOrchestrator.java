package org.spark.ai.gourmetagent.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class GourmetOrchestrator {
    public static final int ATTEMPT = 3;
    private final IntentRouter router;
    private final ReservationAgent reservationAgent;
    private final SommelierAgent sommelierAgent;
    private final ConciergeAgent conciergeAgent;
    private final CounselorAgent counselorAgent;


    public Flux<String> chatStream(String userMessage, String conversationId) {
        var first = router.determineWorker(userMessage);
        String workerType = first.selection();

        WorkerResult lastResult = null;

        // 2) 라우터 평가 기반 재시도 루프
        //    (필요하면 3~4로 늘려도 되지만, UX/비용 고려하면 2~3이 보통 적당)


        for (int attempt = 0; attempt < ATTEMPT; attempt++) {

            lastResult = callWorker(workerType, userMessage, conversationId);

            var eval = router.evaluate(userMessage, lastResult);
            if (eval.ok()) {
                // 3) 최종 답변은 counselor가 스트리밍
                return counselorAgent.streamFinal(userMessage, conversationId, lastResult);
            }

            // 4) 의도 불일치 → 다음 워커로 재시도
            workerType = eval.nextSelection();
            if (workerType == null || workerType.isBlank()) {
                // 방어 로직: nextSelection이 비어 있으면 concierge로 폴백
                workerType = "concierge";
            }
        }

        return counselorAgent.streamFinal(userMessage, conversationId, lastResult);
    }

    private WorkerResult callWorker(String workerType, String userMessage, String conversationId) {
        return switch (workerType) {
            case "reservation" -> new WorkerResult("reservation", reservationAgent.process(userMessage, conversationId));
            case "sommelier"  -> new WorkerResult("sommelier", sommelierAgent.process(userMessage, conversationId));
            default           -> new WorkerResult("concierge", conciergeAgent.process(userMessage, conversationId));
        };
    }
}
