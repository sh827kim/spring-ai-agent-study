package org.spark.ai.gourmetagent.repository;

import org.spark.ai.gourmetagent.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // 특정 예약의 주문 내역을 모두 가져오기 : 총액 계산용
    List<OrderItem> findByReservationId(Long reservationId);
    // 특정 예약의 주문 싹 지우기
    void deleteByReservationId(Long reservationId);
    // 삭제 전 존재하는지 확인용
    boolean existsByReservationId(Long reservationId);
    // 존재하는지 확인용
    boolean existsByReservationIdAndMenuName(Long reservationId, String menuName);
    // 특정 예약(ID)의 특정 메뉴(이름)를 찾아서
    List<OrderItem> findByReservationIdAndMenuName(Long reservationId, String menuName);
}
