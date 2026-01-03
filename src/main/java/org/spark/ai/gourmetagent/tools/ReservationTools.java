package org.spark.ai.gourmetagent.tools;

import lombok.RequiredArgsConstructor;
import org.spark.ai.gourmetagent.dto.BookingDTOs;
import org.spark.ai.gourmetagent.service.ReservationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReservationTools {

    private final ReservationService service;
    @Tool(description = "고객의 연락처로 방문 이력을 조회합니다.")
    public String checkCustomerHistory(BookingDTOs.CustomerCheckRequest request) {
        return service.checkCustomer(request.phoneNumber());
    }

    @Tool(description = "날짜, 인원, 선호 좌석에 맞춰 '예약 가능한 테이블 목록'을 조회합니다.")
    public String searchTables(BookingDTOs.TableSearchRequest request) {
        try {
            return service.findAvailableTables(LocalDateTime.parse(request.dateTime()), request.partySize(), request.preferredType());
        } catch (Exception e) {
            return "오류: 날짜 형식이 올바르지 않습니다.";
        }
    }

    @Tool(description = "최종적으로 예약을 생성합니다.")
    public String bookTable(BookingDTOs.CreateReservationRequest request) {
        return service.createReservation(request.customerName(), request.phoneNumber(), LocalDateTime.parse(request.dateTime()), request.tableId(), request.partySize(), request.allergies(), request.specialRequests());
    }

    @Tool(description = "예정된 예약을 취소합니다.")
    public String cancelReservation(BookingDTOs.CancelReservationRequest request) {
        return service.cancelReservation(request.phoneNumber(), request.reservationId());
    }

    @Tool(description = "나의 예약 목록을 조회합니다.")
    public String checkMyBookings(BookingDTOs.MyBookingRequest request) {
        return service.getMyBookings(request.phoneNumber());
    }
}
