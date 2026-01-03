package org.spark.ai.gourmetagent.repository;

import org.spark.ai.gourmetagent.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    //  [핵심 로직: 겹치는 예약 찾기]
    @Query("""
            SELECT
            r.restaurantTable.id
            FROM Reservation r
            WHERE r.reservationTime < :endTime
            AND r.reservationTime >= :startTimeMinus2Hours
            AND r.status = 'CONFIRMED'
            """)
    List<Long> findBookedTableIds(
            @Param("startTimeMinus2Hours") java.time.LocalDateTime startTimeMinus2Hours,
            @Param("endTime") java.time.LocalDateTime endTime
    );

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.customer.phoneNumber = :phoneNumber
            AND r.reservationTime > CURRENT_TIMESTAMP
            AND r.status = 'CONFIRMED'
            """)
    List<Reservation> findUpcomingReservations(@Param("phoneNumber") String phoneNumber);
}
