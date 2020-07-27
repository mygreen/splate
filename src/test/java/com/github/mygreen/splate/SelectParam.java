package com.github.mygreen.splate;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SelectParam {

    private BigDecimal salaryMin;

    private BigDecimal salaryMax;
}
