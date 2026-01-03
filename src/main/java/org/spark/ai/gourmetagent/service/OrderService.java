package org.spark.ai.gourmetagent.service;


import lombok.RequiredArgsConstructor;
import org.spark.ai.gourmetagent.dto.BookingDTOs;
import org.spark.ai.gourmetagent.entity.Menu;
import org.spark.ai.gourmetagent.entity.OrderItem;
import org.spark.ai.gourmetagent.entity.Reservation;
import org.spark.ai.gourmetagent.repository.MenuRepository;
import org.spark.ai.gourmetagent.repository.OrderItemRepository;
import org.spark.ai.gourmetagent.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final MenuRepository menuRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public String getMenuBoard() {
        List<Menu> menus = menuRepository.findAll();
        if (menus.isEmpty()) {
            return "현재 준비된 메뉴가 없습니다.";
        }
        return menus.stream()
                .collect(Collectors.groupingBy(Menu::getCategory))
                /*
                KEY: "MAIN" -> VALUE: [티본 스테이크, 파스타...]
                KEY: "WINE" -> VALUE: [샴페인, 레드와인...]
                 */
                .entrySet().stream()
                .map(entry -> {
                    String category = entry.getKey(); // MAIN
                    String items = entry.getValue().stream()
                            .map(m -> String.format("- %s: %s원", m.getName(), NumberFormat.getInstance().format(m.getPrice())))
                            .collect(Collectors.joining("\n"));
                    return String.format("[%s]\n%s", category, items);
                })
                .collect(Collectors.joining("\n\n"));
    }

    // 2. 견적(Pre-calculation) 계산
    @Transactional(readOnly = true)
    public String calculateEstimate(List<BookingDTOs.OrderItemRequest> orderItems) {
        long totalAmount = 0;
        StringBuilder receipt = new StringBuilder();
        receipt.append("요청하신 메뉴의 견적입니다:\n");

        for (BookingDTOs.OrderItemRequest item : orderItems) {
            String menuName = item.menuName();
            int quantity = item.quantity();

            Menu menu = menuRepository.findByName(menuName).orElse(null);

            if (menu == null) {
                receipt.append(String.format("- [X] %s: 메뉴 정보 없음\n", menuName));
                continue;
            }

            long itemTotal = (long) menu.getPrice() * quantity;
            totalAmount += itemTotal;

            receipt.append(String.format("- %s %d개: %s원\n",
                    menuName, quantity, NumberFormat.getInstance().format(itemTotal)));
        }

        receipt.append("--------------------\n");
        receipt.append(String.format("총 예상 금액: %s원", NumberFormat.getInstance().format(totalAmount)));

        return receipt.toString();
    }

    @Transactional
    public String addOrderToReservation(Long reservationId, List<BookingDTOs.OrderItemRequest> items) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));

        long totalAmount = 0;
        int count = 0;

        for (BookingDTOs.OrderItemRequest item : items) {
            Menu menu = menuRepository.findByName(item.menuName()).orElse(null);
            if (menu != null) {
                OrderItem order = OrderItem.builder()
                        .reservation(res)
                        .menu(menu)
                        .quantity(item.quantity())
                        .request(item.request())
                        .build();
                orderItemRepository.save(order); // insert

                totalAmount += (long) menu.getPrice() * item.quantity();
                count++;
            }
        }

        String formattedPrice = NumberFormat.getInstance().format(totalAmount);
        return String.format(
                "예약(#%d)에 메뉴 %d건이 정상적으로 추가되었습니다.\n현재 추가된 주문의 총 금액은 [%s원]입니다.",
                reservationId, count, formattedPrice
        );
    }
    // 4. 주문 내역 조회
    @Transactional(readOnly = true)
    public String getOrderHistory(Long reservationId) {
        if (!reservationRepository.existsById(reservationId)) {
            return "존재하지 않는 예약 번호입니다.";
        }

        List<OrderItem> orders = orderItemRepository.findByReservationId(reservationId);

        if (orders.isEmpty()) {
            return String.format("예약번호 #%d에 등록된 주문 내역이 없습니다.", reservationId);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🧾 예약번호 #%d의 주문 내역입니다.\n", reservationId));
        sb.append("----------------\n");

        long total = 0;
        for (OrderItem item : orders) {
            long price = (long) item.getMenu().getPrice() * item.getQuantity();
            total += price;
            sb.append(String.format("- %s %d개: %s원\n",
                    item.getMenu().getName(),
                    item.getQuantity(),
                    NumberFormat.getInstance().format(price)));
        }

        sb.append("----------------\n");
        sb.append(String.format("총 합계: %s원", NumberFormat.getInstance().format(total)));

        return sb.toString();
    }
    // 5. 전체 주문 취소
    @Transactional
    public String cancelOrder(Long reservationId) {
        if (!orderItemRepository.existsByReservationId(reservationId)) {
            return String.format("예약번호 #%d에는 취소할 주문 내역이 없습니다.", reservationId);
        }
        orderItemRepository.deleteByReservationId(reservationId);
        return String.format("예약번호 #%d의 모든 선주문 내역이 정상적으로 취소되었습니다. (예약은 유지됩니다)", reservationId);
    }
    // 6. 특정 메뉴 취소
    @Transactional
    public String removeMenuItem(Long reservationId, String menuName) {
        // 1. 이름으로 주문 내역을 찾습니다. (Bridge)
        List<OrderItem> items = orderItemRepository.findByReservationIdAndMenuName(reservationId, menuName);

        if (items.isEmpty()) {
            return String.format("예약(#%d)에 주문된 '%s' 메뉴가 없습니다.", reservationId, menuName);
        }

        // 2. 삭제 로직 (정책 결정)
        // 상황: 고객이 "샴페인 취소해"라고 했는데, 샴페인을 2병 시켜놨다면?
        // 정책 A: "그 메뉴 싹 다 지워" (현재 채택)
        // 정책 B: "하나만 줄여" (더 복잡한 로직 필요)

        // 여기서는 안전하게 리스트 전체(해당 메뉴 전체)를 삭제하거나,
        // AI가 "1개만 취소해"라고 수량을 명시하지 않았으므로 해당 품목 전체 취소로 간주합니다.

        orderItemRepository.deleteAll(items); // ★ 가져온 엔티티(ID 보유)를 삭제합니다.

        return String.format("예약(#%d)에서 '%s' 메뉴(총 %d건)를 정상적으로 취소했습니다.",
                reservationId, menuName, items.size());
    }
}
