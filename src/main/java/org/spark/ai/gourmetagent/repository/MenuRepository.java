package org.spark.ai.gourmetagent.repository;

import org.spark.ai.gourmetagent.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    // 이름으로 메뉴 정보(가격 등)를 찾기 위함
    Optional<Menu> findByName(String name);
}
