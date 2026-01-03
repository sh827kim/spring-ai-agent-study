package org.spark.ai.gourmetagent.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String phoneNumber; // 고객 식별자 (전화번호가 ID 역할)
    private String name;
    private Integer visitCount; // 방문 횟수 (AI가 단골 손님을 알아보는 기준)
    private String memo;    // 특이사항 (예: "레드 와인 선호", "창가석만 고집함")
    public void addVisitCount() {
        if (this.visitCount == null) {
            this.visitCount = 0;
        }
        this.visitCount += 1;
    }
}
