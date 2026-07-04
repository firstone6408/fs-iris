package com.iris.backend.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.iris.backend.context.user.UserContextProvider;
import com.iris.backend.dto.user.UpdateUserRequestDTO;
import com.iris.backend.dto.user.UserResponseDTO;
import com.iris.backend.entity.UserEntity;
import com.iris.backend.repository.UserRepository;
import com.iris.backend.service.auth.exception.EmailAlreadyTakenException;
import com.iris.backend.service.user.implement.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserContextProvider userContextProvider;

    @InjectMocks private UserServiceImpl userService;

    private UserEntity makeUser(String id, String email, String name) {
        UserEntity u = new UserEntity();
        u.setId(id);
        u.setEmail(email);
        u.setName(name);
        u.setDisplayName(name);
        u.setPasswordHash("$2a$hashed");
        return u;
    }

    // ---- getAll ----

    @Test
    void getAll_returnsMappedDTOsInOrder() {
        UserEntity alice = makeUser("1", "alice@example.com", "Alice");
        UserEntity bob = makeUser("2", "bob@example.com", "Bob");
        when(userRepository.findAllByOrderByEmailAsc()).thenReturn(List.of(alice, bob));

        List<UserResponseDTO> result = userService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("alice@example.com");
        assertThat(result.get(1).getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void getAll_emptyTable_returnsEmptyList() {
        when(userRepository.findAllByOrderByEmailAsc()).thenReturn(List.of());

        assertThat(userService.getAll()).isEmpty();
    }

    // ---- getCurrentUser ----

    @Test
    void getCurrentUser_returnsCurrentUserAsDTO() {
        UserEntity user = makeUser("uuid-1", "alice@example.com", "Alice");
        when(userContextProvider.getCurrentUser()).thenReturn(user);

        UserResponseDTO result = userService.getCurrentUser();

        assertThat(result.getId()).isEqualTo("uuid-1");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getName()).isEqualTo("Alice");
    }

    // ---- update ----

    @Test
    void update_name_updatesOnlyName() {
        UserEntity user = makeUser("1", "alice@example.com", "Alice");
        when(userContextProvider.getCurrentUser()).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserRequestDTO req = mock(UpdateUserRequestDTO.class);
        when(req.getEmail()).thenReturn(null);
        when(req.getName()).thenReturn("Alicia");
        when(req.getDisplayName()).thenReturn(null);
        when(req.getAvatarUrl()).thenReturn(null);
        when(req.getPassword()).thenReturn(null);

        UserResponseDTO result = userService.update(req);

        assertThat(result.getName()).isEqualTo("Alicia");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        verify(userRepository).save(user);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void update_displayName_updatesOnlyDisplayName() {
        UserEntity user = makeUser("1", "alice@example.com", "Alice");
        when(userContextProvider.getCurrentUser()).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserRequestDTO req = mock(UpdateUserRequestDTO.class);
        when(req.getEmail()).thenReturn(null);
        when(req.getName()).thenReturn(null);
        when(req.getDisplayName()).thenReturn("Ally");
        when(req.getAvatarUrl()).thenReturn(null);
        when(req.getPassword()).thenReturn(null);

        userService.update(req);

        assertThat(user.getDisplayName()).isEqualTo("Ally");
    }

    @Test
    void update_email_newEmail_updatesSuccessfully() {
        UserEntity user = makeUser("1", "alice@example.com", "Alice");
        when(userContextProvider.getCurrentUser()).thenReturn(user);
        when(userRepository.findByEmail("new@example.com")).thenReturn(null);
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserRequestDTO req = mock(UpdateUserRequestDTO.class);
        when(req.getEmail()).thenReturn("new@example.com");
        when(req.getName()).thenReturn(null);
        when(req.getDisplayName()).thenReturn(null);
        when(req.getAvatarUrl()).thenReturn(null);
        when(req.getPassword()).thenReturn(null);

        UserResponseDTO result = userService.update(req);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void update_email_sameOwnEmail_doesNotThrow() {
        UserEntity user = makeUser("1", "alice@example.com", "Alice");
        when(userContextProvider.getCurrentUser()).thenReturn(user);
        // findByEmail returns same entity — same ID → should not throw
        when(userRepository.findByEmail("alice@example.com")).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserRequestDTO req = mock(UpdateUserRequestDTO.class);
        when(req.getEmail()).thenReturn("alice@example.com");
        when(req.getName()).thenReturn(null);
        when(req.getDisplayName()).thenReturn(null);
        when(req.getAvatarUrl()).thenReturn(null);
        when(req.getPassword()).thenReturn(null);

        assertThatNoException().isThrownBy(() -> userService.update(req));
    }

    @Test
    void update_email_takenByOtherUser_throwsEmailAlreadyTakenException() {
        UserEntity current = makeUser("1", "alice@example.com", "Alice");
        UserEntity other = makeUser("2", "bob@example.com", "Bob");
        when(userContextProvider.getCurrentUser()).thenReturn(current);
        when(userRepository.findByEmail("bob@example.com")).thenReturn(other);

        UpdateUserRequestDTO req = mock(UpdateUserRequestDTO.class);
        when(req.getEmail()).thenReturn("bob@example.com");

        assertThatThrownBy(() -> userService.update(req))
                .isInstanceOf(EmailAlreadyTakenException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_password_reHashesBeforeSave() {
        UserEntity user = makeUser("1", "alice@example.com", "Alice");
        when(userContextProvider.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.encode("newpass")).thenReturn("$2a$newHash");
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserRequestDTO req = mock(UpdateUserRequestDTO.class);
        when(req.getEmail()).thenReturn(null);
        when(req.getName()).thenReturn(null);
        when(req.getDisplayName()).thenReturn(null);
        when(req.getAvatarUrl()).thenReturn(null);
        when(req.getPassword()).thenReturn("newpass");

        userService.update(req);

        assertThat(user.getPasswordHash()).isEqualTo("$2a$newHash");
        verify(passwordEncoder).encode("newpass");
    }

    @Test
    void update_allNullFields_savesWithoutModifications() {
        UserEntity user = makeUser("1", "alice@example.com", "Alice");
        when(userContextProvider.getCurrentUser()).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserRequestDTO req = mock(UpdateUserRequestDTO.class);
        when(req.getEmail()).thenReturn(null);
        when(req.getName()).thenReturn(null);
        when(req.getDisplayName()).thenReturn(null);
        when(req.getAvatarUrl()).thenReturn(null);
        when(req.getPassword()).thenReturn(null);

        userService.update(req);

        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getName()).isEqualTo("Alice");
        verify(userRepository).save(user);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void update_avatarUrl_updatesAvatarUrl() {
        UserEntity user = makeUser("1", "alice@example.com", "Alice");
        when(userContextProvider.getCurrentUser()).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserRequestDTO req = mock(UpdateUserRequestDTO.class);
        when(req.getEmail()).thenReturn(null);
        when(req.getName()).thenReturn(null);
        when(req.getDisplayName()).thenReturn(null);
        when(req.getAvatarUrl()).thenReturn("https://example.com/avatar.png");
        when(req.getPassword()).thenReturn(null);

        userService.update(req);

        assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
    }
}
