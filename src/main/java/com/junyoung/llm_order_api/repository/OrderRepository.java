package com.junyoung.llm_order_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junyoung.llm_order_api.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
