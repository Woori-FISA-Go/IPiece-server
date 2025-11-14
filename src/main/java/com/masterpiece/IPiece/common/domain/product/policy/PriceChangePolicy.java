package com.masterpiece.IPiece.common.domain.product.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PriceChangePolicy {
    private PriceChangePolicy() {}

    public static double changeRate(long current, long prevClose) {
        if (prevClose <= 0) return 0.0;
        BigDecimal diff = BigDecimal.valueOf(current).subtract(BigDecimal.valueOf(prevClose));
        BigDecimal rate = diff
                .divide(BigDecimal.valueOf(prevClose), 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return rate.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
