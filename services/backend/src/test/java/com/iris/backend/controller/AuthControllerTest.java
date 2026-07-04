package com.iris.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class AuthControllerTest {

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

    // ---- POST /api/auth/register ----

    @Test
    void register_validRequest_returns201WithUser() throws Exception {
        String body = """
                {"email":"alice@example.com","name":"Alice","password":"pass1234"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.email").value("alice@example.com"))
                .andExpect(jsonPath("$.data.name").value("Alice"))
                .andExpect(jsonPath("$.data.id").isNotEmpty());
    }

    @Test
    void register_withDisplayName_persistsDisplayName() throws Exception {
        String body = """
                {"email":"alice@example.com","name":"Alice","displayName":"Ally","password":"pass1234"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.displayName").value("Ally"));
    }

    @Test
    void register_missingEmail_returns400() throws Exception {
        String body = """
                {"name":"Alice","password":"pass1234"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void register_invalidEmailFormat_returns400() throws Exception {
        String body = """
                {"email":"not-an-email","name":"Alice","password":"pass1234"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void register_missingName_returns400() throws Exception {
        String body = """
                {"email":"alice@example.com","password":"pass1234"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void register_missingPassword_returns400() throws Exception {
        String body = """
                {"email":"alice@example.com","name":"Alice"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        createUserAndGetToken("alice@example.com", "Alice");

        String body = """
                {"email":"alice@example.com","name":"Alice2","password":"pass1234"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.status").value(409));
    }

    // ---- POST /api/auth/login ----

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        createUserAndGetToken("alice@example.com", "Alice");

        String body = """
                {"email":"alice@example.com","password":"password123"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        createUserAndGetToken("alice@example.com", "Alice");

        String body = """
                {"email":"alice@example.com","password":"wrongPassword"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_nonExistentEmail_returns401() throws Exception {
        String body = """
                {"email":"nobody@example.com","password":"pass1234"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void login_emptyEmail_returns400() throws Exception {
        String body = """
                {"email":"","password":"pass1234"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ---- GET /api/auth/me ----

    @Test
    void me_withValidToken_returnsCurrentUser() throws Exception {
        String token = createUserAndGetToken("alice@example.com", "Alice");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.email").value("alice@example.com"))
                .andExpect(jsonPath("$.data.name").value("Alice"));
    }

    @Test
    void me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void me_withInvalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer this.is.not.valid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void me_withMalformedHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "NotBearer token"))
                .andExpect(status().isUnauthorized());
    }
}
