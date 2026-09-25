/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.funInterface;

import java.math.BigDecimal;

@FunctionalInterface
public interface ToBigDecimalFunction<T> {
    BigDecimal applyAsBigDecimal(T value);
}
