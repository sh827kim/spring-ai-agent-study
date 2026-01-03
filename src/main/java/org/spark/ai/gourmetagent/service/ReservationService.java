package org.spark.ai.gourmetagent.service;

import lombok.RequiredArgsConstructor;
import org.spark.ai.gourmetagent.domain.ReservationStatus;
import org.spark.ai.gourmetagent.domain.TableType;
import org.spark.ai.gourmetagent.entity.Customer;
import org.spark.ai.gourmetagent.entity.Reservation;
import org.spark.ai.gourmetagent.entity.RestaurantTable;
import org.spark.ai.gourmetagent.repository.CustomerRepository;
import org.spark.ai.gourmetagent.repository.OrderItemRepository;
import org.spark.ai.gourmetagent.repository.ReservationRepository;
import org.spark.ai.gourmetagent.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final RestaurantTableRepository restaurantTableRepository;
    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;


    public String findAvailableTables(LocalDateTime time, int size, String typeStr) {
        // 1. 물리적 조건(인원, 타입)에 맞는 테이블 후보 조회 (집합 A)
        List<RestaurantTable> candidates;
        if (typeStr != null) {
            try {
                candidates = restaurantTableRepository.findByCapacityGreaterThanEqualAndType(size, TableType.valueOf(typeStr));
            } catch (IllegalArgumentException e) {
                return "잘못된 좌석 타입입니다. WINDOW, ROOM, HALL 중 하나여야 합니다.";
            }
        } else {
            candidates = restaurantTableRepository.findByCapacityGreaterThanEqual(size);
        }

        // 2. 해당 시간대에 이미 예약된 테이블 ID 조회 (집합 B)
        // 식사 시간 2시간 가정: 내 식사 시작 시간의 앞뒤 2시간을 검사해야 함
        LocalDateTime checkStartTime = time.minusHours(2); // 2시간 전
        LocalDateTime checkEndTime = time.plusHours(2);    // 2시간 후

        // 앞서 구현한 Repository의 쿼리를 호출하여 '겹치는 테이블 ID'를 가져옴
        List<Long> bookedIds = reservationRepository.findBookedTableIds(checkStartTime, checkEndTime);

        // 3. 차집합 연산 (전체 후보 - 예약된 것 = 빈 테이블)
        List<RestaurantTable> available = candidates.stream()
                .filter(t -> !bookedIds.contains(t.getId()))
                .toList();

        if (available.isEmpty()) return "죄송합니다. 해당 조건에 맞는 빈 테이블이 없습니다.";

        // 4. AI가 읽기 편한 자연어로 반환
        // 예: "- [ID:1] WINDOW타입 (4인석)"
        StringBuilder sb = new StringBuilder("예약 가능 테이블 목록:\n");
        for (RestaurantTable t : available) {
            sb.append(String.format("- [ID:%d] %s타입 (%d인석)\n", t.getId(), t.getType(), t.getCapacity()));
        }
        return sb.toString();
    }

    @Transactional
    public String createReservation(String name, String phone, LocalDateTime time, Long tableId, int size, String allergies, String specialRequests) {
        // 테이블 유효성 재검증 (동시성 문제 최소화)
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테이블 ID입니다."));

        // CRM: 고객 조회 또는 신규 생성 (Upsert)
        Customer customer = customerRepository.findByPhoneNumber(phone)
                .orElseGet(() -> {
                    // 신규 고객일 경우 실행되는 로직
                    // 1. 초기 메모 생성: 이번 예약의 알레르기 정보를 평생 메모에 기록
                    String initialMemo = "신규 등록";
                    if (allergies != null && !allergies.equals("없음") && !allergies.equals("None")) {
                        initialMemo = "주의: " + allergies;
                    }
                    // 2. 신규 고객 저장 및 반환
                    return customerRepository.save(Customer.builder()
                            .name(name)
                            .phoneNumber(phone)
                            .visitCount(0)
                            .memo(initialMemo)
                            .build());
                });

        // 방문 횟수 증가 (기존 고객이든 신규 고객이든 +1)
        customer.addVisitCount();

        // 예약 정보 저장
        Reservation res = Reservation.builder()
                .customer(customer) // PK
                .restaurantTable(table) // PK
                .reservationTime(time)
                .partySize(size)
                .allergies(allergies)
                .status(ReservationStatus.CONFIRMED) // 확정 상태로 저장
                .specialRequests(specialRequests) // 저장!
                .build();

        reservationRepository.save(res);

        return String.format("예약이 확정되었습니다! \n예약번호 [#%d]. %s님을 %d번 테이블(%s)로 모시겠습니다.",
                res.getId(), name, table.getId(), table.getType());
    }
    // [Logic 3] CRM 조회: 단순 식별용
    public String checkCustomer(String phone) {
        return customerRepository.findByPhoneNumber(phone)
                .map(c -> String.format("기존 고객입니다. 이름: %s, 방문횟수: %d회. (VIP 여부 확인 필요)", c.getName(), c.getVisitCount()))
                .orElse("신규 고객입니다.");
    }

    // [Logic 4] 예약 취소: 모호성 해결 및 상태 변경
    @Transactional
    public String cancelReservation(String phoneNumber, Long reservationId) {
        // 1. 예약 ID로 특정 예약 조회
        Optional<Reservation> reservationOptional=reservationRepository.findById(reservationId);
        if(reservationOptional.isEmpty()){
            return  "해당 예약 번호의 예약 내역이 없습니다.";
        }
        Reservation reservation=reservationOptional.get();
        // 2. 예약자 전화번호 확인 및 미래 예약인지 확인
        if(!reservation.getCustomer().getPhoneNumber().equals(phoneNumber) ||
                reservation.getReservationTime().isBefore(LocalDateTime.now())){
            return  "예약을 취소할 수 없습니다. 예약 정보를 다시 확인해주세요.";
        }
        // 3. 예약 취소 처리
        orderItemRepository.deleteByReservationId(reservation.getId());
        reservation.cancel(); // 상태 변경 (Soft Delete)

        return String.format("예약이 정상적으로 취소되었습니다.\n(함께 주문하신 메뉴 내역도 모두 삭제되었습니다.)\n[취소 내역] 날짜: %s, 인원: %d명",
                reservation.getReservationTime().toString().replace("T", " "),
                reservation.getPartySize());
    }

    // [Logic 5] 예약 조회 (단순 확인용)
    @Transactional(readOnly = true)
    public String getMyBookings(String phoneNumber) {
        // 이미 만들어둔 쿼리 재사용 (미래의 확정된 예약만 조회)
        List<Reservation> bookings = reservationRepository.findUpcomingReservations(phoneNumber);

        if (bookings.isEmpty()) {
            return "해당 번호로 잡혀있는 예정된 예약이 없습니다.";
        }

        StringBuilder sb = new StringBuilder("고객님의 예약 내역입니다:\n");
        for (Reservation r : bookings) {
            sb.append(String.format("- %s, %d명, %s타입 테이블 (예약번호 #%d)\n",
                    r.getReservationTime().toString().replace("T", " "), // 보기 좋게 포맷팅
                    r.getPartySize(),
                    r.getRestaurantTable().getType(),
                    r.getId()));
        }
        return sb.toString();
    }
}
