package com.sport.ecommerce.modules.user.service;

import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.modules.user.dto.response.CustomerDetailResponse;
import com.sport.ecommerce.modules.user.dto.response.CustomerSummaryResponse;

public interface CustomerService {
    PageResponse<CustomerSummaryResponse> getCustomers(int page, int size, String keyword, String status);
    CustomerDetailResponse getCustomerById(Long id);
}
