package org.spark.ai.gourmetagent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public class BookingDTOs {
    public record CustomerCheckRequest(
            @JsonPropertyDescription("고객 식별을 위한 휴대폰 번호 (010-xxxx-xxxx)")
            String phoneNumber
    ) {}
    public record TableSearchRequest(
            @JsonPropertyDescription("예약 희망 날짜 및 시간 (ISO-8601, 예: 2025-12-25T19:00:00)")
            String dateTime,
            @JsonPropertyDescription("방문 인원 수")
            Integer partySize,
            @JsonPropertyDescription("선호 좌석 타입 (WINDOW, ROOM, HALL). 상관없으면 null")
            String preferredType
    ) {}
    public record CreateReservationRequest(
            @JsonPropertyDescription("고객 성함") String customerName,
            @JsonPropertyDescription("고객 연락처") String phoneNumber,
            @JsonPropertyDescription("확정된 예약 시간") String dateTime,
            @JsonPropertyDescription("선택한 테이블 번호 (검색된 ID 중 하나)")
            Long tableId,
            @JsonPropertyDescription("인원 수") Integer partySize,
            @JsonPropertyDescription("알레르기 정보 (없으면 '없음')") String allergies,
            @JsonPropertyDescription("기념일, 생일 등 고객의 특별 요청사항 (없으면 null)")
            String specialRequests
    ) {}
    public record CancelReservationRequest(
            @JsonPropertyDescription("예약자 연락처 (010-xxxx-xxxx)")
            String phoneNumber,
            @JsonPropertyDescription("취소할 예약 ID") Long reservationId
    ) {}
    public record MyBookingRequest(
            @JsonPropertyDescription("예약자 연락처 (010-xxxx-xxxx)")
            String phoneNumber
    ) {}
    public record OrderItemRequest(
            @JsonPropertyDescription("메뉴 이름") String menuName,
            @JsonPropertyDescription("수량") int quantity,
            @JsonPropertyDescription("메뉴별 요청사항 (예: 굽기 정도, 옵션 등)")
            String request
    ) {}
    public record EstimateRequest(
            @JsonPropertyDescription("주문할 메뉴 목록")
            List<OrderItemRequest> orderItems //  List로 변경!
    ) {}
    public record AddOrderRequest(
            @JsonPropertyDescription("메뉴를 추가할 예약의 번호(ID)") Long reservationId,
            @JsonPropertyDescription("추가할 메뉴 목록") List<OrderItemRequest> orderItems
    ) {}
    public record OrderHistoryRequest(
            @JsonPropertyDescription("주문 내역을 조회할 예약 번호(ID)") Long reservationId
    ) {}
    public record CancelOrderRequest(
            @JsonPropertyDescription("주문을 취소할 예약 번호(ID)") Long reservationId
    ) {}
    public record CancelMenuItemRequest(
            @JsonPropertyDescription("예약 번호(ID)") Long reservationId,
            @JsonPropertyDescription("취소할 메뉴 이름 (예: 샴페인)") String menuName
    ) {}
}
