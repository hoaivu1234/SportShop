package com.sport.ecommerce.modules.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerDetailResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String initials;
    private String tier;
    private String tierClass;
    private long orderCount;
    private BigDecimal ltv;
    private String riskLevel;
    private LocalDateTime joinedDate;
    private String lastSeen;
    private List<ActivityItem> recentActivity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItem {
        private String icon;
        private String text;
        private String time;
        private String color;
    }
}
