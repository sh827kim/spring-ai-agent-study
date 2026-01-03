package org.spark.ai.gourmetagent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.spark.ai.gourmetagent.domain.TableType;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTable {
    @Id
    private Long id; // 테이블 번호 (1, 2, 3...) - 자동 생성 아님

    private Integer capacity; // 최대 수용 인원 (4인석, 6인석 등)

    @Enumerated(EnumType.STRING)
    private TableType type; // WINDOW, ROOM, HALL
}
