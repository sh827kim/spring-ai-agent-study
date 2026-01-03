package org.spark.ai.gourmetagent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {
    @Id
    private Long id; // 메뉴 번호 (예: 1, 2, 3...) - 관리 편의상 수동 지정 추천

    private String name; // 메뉴명 (예: "티본 스테이크") - RAG 검색 결과와 매칭될 키

    private int price;   // 가격 (예: 150000) - 계산 로직의 핵심

    private String category; // (선택) MAIN, WINE, DESSERT 등
}
