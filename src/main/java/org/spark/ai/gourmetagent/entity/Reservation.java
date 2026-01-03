package org.spark.ai.gourmetagent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.spark.ai.gourmetagent.domain.ReservationStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime reservationTime; // 예약 일시
    // 어떤 고객의 예약인가?
    @ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "customer_id") // FK
    private Customer customer;
    // 어떤 테이블을 점유했는가?
    @ManyToOne(fetch = FetchType.LAZY)
    //  FK : restaurantTable_id
    private RestaurantTable restaurantTable;
    private int partySize; // 실제 방문 인원
    private String allergies; // 알레르기 정보 (안전 점검 필수 항목)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status; // CONFIRMED or CANCELLED
    // [New] 기념일, 유아 동반 여부 등을 적는 메모장
    private String specialRequests;

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }
}
