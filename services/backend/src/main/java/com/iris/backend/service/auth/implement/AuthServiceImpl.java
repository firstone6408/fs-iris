package com.iris.backend.service.auth.implement;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iris.backend.dto.auth.LoginRequestDTO;
import com.iris.backend.dto.auth.LoginResponseDTO;
import com.iris.backend.dto.auth.RegisterRequestDTO;
import com.iris.backend.dto.user.UserResponseDTO;
import com.iris.backend.entity.UserEntity;
import com.iris.backend.repository.UserRepository;
import com.iris.backend.service.auth.contract.AuthService;
import com.iris.backend.service.auth.exception.EmailAlreadyTakenException;
import com.iris.backend.service.auth.exception.InvalidCredentialsException;
import com.iris.backend.util.Jwt;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Jwt jwt;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, Jwt jwt) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
    }

    @Override
    @Transactional
    public UserResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new EmailAlreadyTakenException(request.getEmail());
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setDisplayName(request.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return UserResponseDTO.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        UserEntity user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return LoginResponseDTO.fromToken(jwt.generateToken(user.getId()));
    }
}
