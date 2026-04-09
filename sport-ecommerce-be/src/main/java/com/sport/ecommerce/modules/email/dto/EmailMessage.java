package com.sport.ecommerce.modules.email.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessage {
    private String from;
    private String to;
    private String type;
    private Long orderId;
    private String customerName;
    private BigDecimal totalAmount;
}
