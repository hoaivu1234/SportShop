package com.sport.ecommerce.modules.user.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.modules.user.dto.response.CustomerDetailResponse;
import com.sport.ecommerce.modules.user.dto.response.CustomerSummaryResponse;
import com.sport.ecommerce.modules.user.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.ADMIN_PREFIX + "/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CustomerSummaryResponse>>> getCustomers(
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "10") int    size,
            @RequestParam(required = false)    String keyword,
            @RequestParam(required = false)    String status) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.getCustomers(page, size, keyword, status)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> getCustomerById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerById(id)));
    }
}
