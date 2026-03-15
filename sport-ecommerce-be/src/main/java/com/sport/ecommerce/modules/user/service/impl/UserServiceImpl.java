package com.sport.ecommerce.modules.user.service.impl;

import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.common.enums.UserStatus;
import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.user.dto.request.CreateUserRequest;
import com.sport.ecommerce.modules.user.dto.request.UpdateUserRequest;
import com.sport.ecommerce.modules.user.dto.response.UserResponse;
import com.sport.ecommerce.modules.user.entity.Role;
import com.sport.ecommerce.modules.user.entity.User;
import com.sport.ecommerce.modules.user.mapper.UserMapper;
import com.sport.ecommerce.modules.user.repository.RoleRepository;
import com.sport.ecommerce.modules.user.repository.UserRepository;
import com.sport.ecommerce.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(Pageable pageable) {
        Page<UserResponse> page = userRepository.findAll(pageable)
                .map(userMapper::toResponse);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "User " + " already exists with " + "email: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        // Role
        Role role = roleRepository.findByName("ROLE_CUSTOMER");
        HashSet<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        user.setStatus(UserStatus.ACTIVE.name());

        User saved = userRepository.save(user);
        log.info("Created user with id: {}", saved.getId());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserById(id);

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        User saved = userRepository.save(user);
        log.info("Updated user with id: {}", saved.getId());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);
        user.setStatus(UserStatus.INACTIVE.name());
        userRepository.save(user);
        log.info("Soft-deleted user with id: {}", id);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "User " + " not found with id: " + id));
    }
}
