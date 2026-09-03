package com.junyoung.llm_order_api.order.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.UNASSIGNED;

    @Column(name = "start_lat", nullable = false)
    private Double startLatitude;

    @Column(name = "start_lon", nullable = false)
    private Double startLongitude;

    @Column(name = "end_lat", nullable = false)
    private Double endLatitude;

    @Column(name = "end_lon", nullable = false)
    private Double endLongitude;

    @Column(name = "distance", nullable = false)
    private Integer distance;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private Instant updatedAt;

    public static Order create(Double startLatitude, Double startLongitude, Double endLatitude, Double endLongitude,
            Integer distance) {
        Order order = new Order();
        order.setStartLatitude(startLatitude);
        order.setStartLongitude(startLongitude);
        order.setEndLatitude(endLatitude);
        order.setEndLongitude(endLongitude);
        order.setDistance(distance);
        order.setStatus(OrderStatus.UNASSIGNED);
        return order;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Long getId() {
        return id;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public Integer getDistance() {
        return distance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public void setEndLongitude(Double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}
