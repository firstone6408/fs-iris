package com.iris.backend.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.iris.backend.dto.auth.LoginRequestDTO;
import com.iris.backend.dto.auth.LoginResponseDTO;
import com.iris.backend.dto.auth.RegisterRequestDTO;
import com.iris.backend.dto.user.UserResponseDTO;
import com.iris.backend.entity.UserEntity;
import com.iris.backend.repository.UserRepository;
import com.iris.backend.service.auth.exception.EmailAlreadyTakenException;
import com.iris.backend.service.auth.exception.InvalidCredentialsException;
import com.iris.backend.service.auth.implement.AuthServiceImpl;
import com.iris.backend.util.Jwt;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private Jwt jwt;

    @InjectMocks private AuthServiceImpl authService;

    // ---- register ----

    @Test
    void register_newEmail_savesUserAndReturnsDTO() {
        RegisterRequestDTO req = mock(RegisterRequestDTO.class);
        when(req.getEmail()).thenReturn("alice@example.com");
        when(req.getName()).thenReturn("Alice");
        when(req.getDisplayName()).thenReturn("Ally");
        when(req.getPassword()).thenReturn("pass123");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(null);
        when(passwordEncoder.encode("pass123")).thenReturn("$2a$hashed");

        UserEntity saved = new UserEntity();
        saved.setId("uuid-1");
        saved.setEmail("alice@example.com");
        saved.setName("Alice");
        saved.setDisplayName("Ally");
        when(userRepository.save(any(UserEntity.class))).thenReturn(saved);

        UserResponseDTO result = authService.register(req);

        assertThat(result.getId()).isEqualTo("uuid-1");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getName()).isEqualTo("Alice");
        assertThat(result.getDisplayName()).isEqualTo("Ally");
        verify(passwordEncoder).encode("pass123");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void register_duplicateEmail_throwsEmailAlreadyTakenException() {
        RegisterRequestDTO req = mock(RegisterRequestDTO.class);
        when(req.getEmail()).thenReturn("existing@example.com");

        UserEntity existing = new UserEntity();
        existing.setEmail("existing@example.com");
        when(userRepository.findByEmail("existing@example.com")).thenReturn(existing);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(EmailAlreadyTakenException.class);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void register_passwordIsHashedBeforeSave() {
        RegisterRequestDTO req = mock(RegisterRequestDTO.class);
        when(req.getEmail()).thenReturn("bob@example.com");
        when(req.getName()).thenReturn("Bob");
        when(req.getDisplayName()).thenReturn(null);
        when(req.getPassword()).thenReturn("plaintext");

        when(userRepository.findByEmail("bob@example.com")).thenReturn(null);
        when(passwordEncoder.encode("plaintext")).thenReturn("$2a$hashed");

        UserEntity saved = new UserEntity();
        saved.setEmail("bob@example.com");
        saved.setPasswordHash("$2a$hashed");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            assertThat(u.getPasswordHash()).isEqualTo("$2a$hashed");
            assertThat(u.getPasswordHash()).doesNotContain("plaintext");
            return saved;
        });

        authService.register(req);
    }

    // ---- login ----

    @Test
    void login_correctCredentials_returnsToken() {
        LoginRequestDTO req = mock(LoginRequestDTO.class);
        when(req.getEmail()).thenReturn("alice@example.com");
        when(req.getPassword()).thenReturn("pass123");

        UserEntity user = new UserEntity();
        user.setId("uuid-1");
        user.setEmail("alice@example.com");
        user.setPasswordHash("$2a$hashed");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(user);
        when(passwordEncoder.matches("pass123", "$2a$hashed")).thenReturn(true);
        when(jwt.generateToken("uuid-1")).thenReturn("signed.jwt.token");

        LoginResponseDTO result = authService.login(req);

        assertThat(result.getToken()).isEqualTo("signed.jwt.token");
        verify(jwt).generateToken("uuid-1");
    }

    @Test
    void login_emailNotFound_throwsInvalidCredentialsException() {
        LoginRequestDTO req = mock(LoginRequestDTO.class);
        when(req.getEmail()).thenReturn("nobody@example.com");

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(null);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwt, never()).generateToken(any());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsException() {
        LoginRequestDTO req = mock(LoginRequestDTO.class);
        when(req.getEmail()).thenReturn("alice@example.com");
        when(req.getPassword()).thenReturn("wrong");

        UserEntity user = new UserEntity();
        user.setPasswordHash("$2a$hashed");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "$2a$hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwt, never()).generateToken(any());
    }

    @Test
    void login_invalidCredentials_errorMessageIsGeneric() {
        // Message must not reveal whether the email exists (prevents user enumeration)
        LoginRequestDTO req = mock(LoginRequestDTO.class);
        when(req.getEmail()).thenReturn("nobody@example.com");
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(null);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }
}
