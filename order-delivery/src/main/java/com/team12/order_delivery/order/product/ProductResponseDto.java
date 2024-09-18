package com.team12.order_delivery.order.product;

import java.time.LocalDateTime;

public record ProductResponseDto(
        String productId,
        String companyId,
        String hub_id,
        String productName,
        Boolean isDelete,
        LocalDateTime createdAt,
        Long createdBy,
        LocalDateTime updatedAt,
        Long updatedBy,
        LocalDateTime deletedAt,
        Long deletedBy,
        Long quantity
) {
    
}
