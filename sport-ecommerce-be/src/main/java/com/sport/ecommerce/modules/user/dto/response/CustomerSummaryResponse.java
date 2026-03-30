package com.sport.ecommerce.modules.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerSummaryResponse {
    private Long id;
    private String name;
    private String email;
    private String initials;
    private String tier;       // VIP | Regular | New | Inactive
    private String tierClass;  // vip | regular | new | inactive
    private long orderCount;
    private BigDecimal ltv;
    private LocalDateTime joinedDate;
}
