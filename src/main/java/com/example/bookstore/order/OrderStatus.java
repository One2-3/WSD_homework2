package com.example.bookstore.order;

// DB ENUM(order_status)와 동일하게 소문자
public enum OrderStatus {
    pending, paid, shipped, delivered, cancelled, refunded
}
