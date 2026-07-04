package com.iris.backend.service.user.implement;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iris.backend.context.user.UserContextProvider;
import com.iris.backend.dto.user.UpdateUserRequestDTO;
import com.iris.backend.dto.user.UserResponseDTO;
import com.iris.backend.entity.UserEntity;
import com.iris.backend.repository.UserRepository;
import com.iris.backend.service.auth.exception.EmailAlreadyTakenException;
import com.iris.backend.service.user.contract.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserContextProvider userContextProvider;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            UserContextProvider userContextProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userContextProvider = userContextProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAll() {
        return userRepository.findAllByOrderByEmailAsc()
                .stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser() {
        return UserResponseDTO.fromEntity(userContextProvider.getCurrentUser());
    }

    @Override
    @Transactional
    public UserResponseDTO update(UpdateUserRequestDTO request) {
        UserEntity user = userContextProvider.getCurrentUser();

        if (request.getEmail() != null) {
            UserEntity existing = userRepository.findByEmail(request.getEmail());
            if (existing != null && !existing.getId().equals(user.getId())) {
                throw new EmailAlreadyTakenException(request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return UserResponseDTO.fromEntity(userRepository.save(user));
    }
}
