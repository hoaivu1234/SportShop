package com.sport.ecommerce.modules.user.mapper;

import com.sport.ecommerce.modules.user.dto.response.UserResponse;
import com.sport.ecommerce.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponse toResponse(User user);
}
