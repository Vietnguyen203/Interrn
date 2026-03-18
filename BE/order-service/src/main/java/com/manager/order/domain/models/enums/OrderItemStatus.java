package com.manager.order.domain.models.enums;

public enum OrderItemStatus {
    PENDING, // Món mới nhận, chờ chế biến
    PREPARING, // Đang chế biến
    READY, // Đã xong, chờ waiter giao
    SERVED // Đã giao cho khách
}
