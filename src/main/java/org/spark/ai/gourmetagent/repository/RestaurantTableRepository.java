package org.spark.ai.gourmetagent.repository;

import org.spark.ai.gourmetagent.domain.TableType;
import org.spark.ai.gourmetagent.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByCapacityGreaterThanEqual(Integer capacity);
    List<RestaurantTable> findByCapacityGreaterThanEqualAndType(Integer capacity, TableType type);
}
