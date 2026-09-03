package com.junyoung.llm_order_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.junyoung.llm_order_api.entity.Order;
import com.junyoung.llm_order_api.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {
  @Modifying(clearAutomatically = true)
  @Query("""
      UPDATE Order o
      SET o.status = :taken,
          o.updatedAt = CURRENT_TIMESTAMP
      WHERE o.id = :id
        AND o.status = :unassigned
      """)
  int takeOrder(
      @Param("id") Long id,
      @Param("taken") OrderStatus taken,
      @Param("unassigned") OrderStatus unassigned);
}
