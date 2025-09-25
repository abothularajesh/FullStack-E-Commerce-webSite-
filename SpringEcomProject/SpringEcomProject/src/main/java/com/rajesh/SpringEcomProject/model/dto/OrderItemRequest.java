package com.rajesh.SpringEcomProject.model.dto;

public record OrderItemRequest(
        int productId,
        int quantity
) {}
