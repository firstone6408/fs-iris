package com.iris.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.iris.backend.entity.UserEntity;
import com.iris.backend.repository.UserRepository;
import com.iris.backend.util.Jwt;

@SpringBootTest
class UserControllerTest {

    @Autowired private WebApplicationContext wac;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private Jwt jwt;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        userRepository.deleteAll();
    }

    /** Creates a user directly in the DB and returns a ready-to-use Bearer token. */
    private String createUserAndGetToken(String email, String name) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        UserEntity saved = userRepository.save(user);
        return "Bearer " + jwt.generateToken(saved.getId());
    }

    // ---- GET /api/users ----

    @Test
    void getAll_withValidToken_returnsListOrderedByEmail() throws Exception {
        String token = createUserAndGetToken("alice@example.com", "Alice");
        createUserAndGetToken("bob@example.com", "Bob");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$.data[1].email").value("bob@example.com"));
    }

    @Test
    void getAll_emptyDatabase_returnsEmptyList() throws Exception {
        String token = createUserAndGetToken("alice@example.com", "Alice");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getAll_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false));
    }

    // ---- PUT /api/users/me ----

    @Test
    void updateMe_name_returns200WithUpdatedName() throws Exception {
        String token = createUserAndGetToken("alice@example.com", "Alice");

        String body = """
                {"name":"Alicia"}
                """;

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.name").value("Alicia"))
                .andExpect(jsonPath("$.data.email").value("alice@example.com"));
    }

    @Test
    void updateMe_displayName_returns200WithUpdatedDisplayName() throws Exception {
        String token = createUserAndGetToken("alice@example.com", "Alice");

        String body = """
                {"displayName":"Ally"}
                """;

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("Ally"));
    }

    @Test
    void updateMe_email_newEmail_returns200() throws Exception {
        String token = createUserAndGetToken("alice@example.com", "Alice");

        String body = """
                {"email":"alice.new@example.com"}
                """;

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("alice.new@example.com"));
    }

    @Test
    void updateMe_invalidEmailFormat_returns400() throws Exception {
        String token = createUserAndGetToken("alice@example.com", "Alice");

        String body = """
                {"email":"not-valid"}
                """;

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void updateMe_emailTakenByOtherUser_returns409() throws Exception {
        String token = createUserAndGetToken("alice@example.com", "Alice");
        createUserAndGetToken("bob@example.com", "Bob");

        String body = """
                {"email":"bob@example.com"}
                """;

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void updateMe_withoutToken_returns401() throws Exception {
        String body = """
                {"name":"Alicia"}
                """;

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
