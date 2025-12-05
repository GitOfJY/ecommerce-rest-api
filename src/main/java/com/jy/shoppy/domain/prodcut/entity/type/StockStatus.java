package com.jy.shoppy.domain.prodcut.entity.type;

public enum StockStatus {
    IN_STOCK,       // 재고 있음
    OUT_OF_STOCK,   // 재고 없음
    LOW_STOCK       // 재고 임박 (5개 미만)
}
