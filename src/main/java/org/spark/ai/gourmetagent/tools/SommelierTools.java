package org.spark.ai.gourmetagent.tools;

import lombok.RequiredArgsConstructor;
import org.spark.ai.gourmetagent.dto.BookingDTOs;
import org.spark.ai.gourmetagent.service.OrderService;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SommelierTools {
    private final OrderService orderService;
    private final VectorStore vectorStore;


    @Tool(description = "전체 메뉴판을 보여줍니다.")
    public String showMenuList() {
        return orderService.getMenuBoard(); // 호출 메서드 변경
    }
    @Tool(description = "주문 예상 견적을 계산합니다.")
    public String getPriceEstimate(BookingDTOs.EstimateRequest request) {
        return orderService.calculateEstimate(request.orderItems());
    }
    @Tool(description = "예약에 메뉴 주문을 추가합니다.")
    public String addOrder(BookingDTOs.AddOrderRequest request) {
        return orderService.addOrderToReservation(request.reservationId(), request.orderItems());
    }
    @Tool(description = "주문 내역을 조회합니다.")
    public String checkOrderedMenu(BookingDTOs.OrderHistoryRequest request) {
        return orderService.getOrderHistory(request.reservationId());
    }
    @Tool(description = "특정 메뉴 하나를 취소합니다.")
    public String removeMenuItem(BookingDTOs.CancelMenuItemRequest request) {
        return orderService.removeMenuItem(request.reservationId(), request.menuName());
    }
    @Tool(description = "전체 주문을 취소합니다.")
    public String cancelOrder(BookingDTOs.CancelOrderRequest request) {
        return orderService.cancelOrder(request.reservationId());
    }
    // 메뉴 추천?
    @Tool(description = "고객이 '메뉴 추천'을 원하거나, 특정 맛/재료를 찾을 때 상세 정보를 검색합니다.")
    public String searchMenuDescriptions(String query) { // SQL(X), SearchRequest
        List<Document> results = this.vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(3).build() // 코사인 유사도
        );
        if (results.isEmpty()) return "관련 정보를 찾을 수 없습니다.";
        return results.stream().map(Document::getText).collect(Collectors.joining("\n"));
    }
}
