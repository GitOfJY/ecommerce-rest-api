package com.jy.shoppy.domain.prodcut.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductSyncResult {
    private final int insertedCount;
    private final int updatedCount;
    private final int deactivatedCount;
    private final int totalProcessed;

    @Override
    public String toString() {
        return String.format("신규: %d, 업데이트: %d, 비활성화: %d, 총 처리: %d",
                insertedCount, updatedCount, deactivatedCount, totalProcessed);
    }
}
