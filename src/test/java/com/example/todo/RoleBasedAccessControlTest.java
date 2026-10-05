package com.example.todo;

import com.example.todo.entity.AppUser;
import com.example.todo.entity.Role;
import com.example.todo.repository.AppUserRepository;
import com.example.todo.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoleBasedAccessControlTest {

    private static final String PASSWORD = "Test@12345";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private String userName;
    private String adminName;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        userName = "user_" + suffix;
        adminName = "admin_" + suffix;
    }

    @AfterEach
    void tearDown() {
        userRepository.findAll().stream()
                .filter(user -> user.getUsername().equals(userName) || user.getUsername().equals(adminName))
                .forEach(userRepository::delete);
    }

    @Test
    @DisplayName("ثبت‌نام توکن معتبر با نقش USER برمی‌گرداند")
    void register_returnsTokenWithUserRole() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCredentials(userName, PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value(userName))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("ثبت‌نام با نام کاربری تکراری کنترل می‌شود")
    void register_duplicateUsername_returnsConflict() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCredentials(userName, PASSWORD))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCredentials(userName, PASSWORD))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("USERNAME_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("ورود با رمز عبور نادرست رد می‌شود")
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        createUser(userName, EnumSet.of(Role.USER));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCredentials(userName, "Wrong@12345"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("BAD_CREDENTIALS"));
    }

    @Test
    @DisplayName("درخواست بدون توکن با 401 رد می‌شود")
    void request_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("درخواست با توکن نامعتبر با 401 رد می‌شود")
    void request_withInvalidToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/todos").header("Authorization", "Bearer not-a-valid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("کاربر واردشده می‌تواند وظایف را ببیند و بسازد")
    void user_canReadAndCreateTodos() throws Exception {
        String token = login(userName, EnumSet.of(Role.USER));

        mockMvc.perform(get("/api/todos").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"کار تست\",\"completed\":false}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("کاربر عادی اجازه حذف وظیفه را ندارد")
    void user_cannotDeleteTodo_returnsForbidden() throws Exception {
        String token = login(userName, EnumSet.of(Role.USER));
        Long todoId = createTodo(token);

        mockMvc.perform(delete("/api/todos/{id}", todoId).header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("کاربر عادی اجازه ویرایش وظیفه را ندارد")
    void user_cannotUpdateTodo_returnsForbidden() throws Exception {
        String token = login(userName, EnumSet.of(Role.USER));
        Long todoId = createTodo(token);

        mockMvc.perform(put("/api/todos/{id}", todoId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"عنوان جدید\",\"completed\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("مدیر اجازه حذف وظیفه را دارد")
    void admin_canDeleteTodo_returnsNoContent() throws Exception {
        String adminToken = login(adminName, EnumSet.of(Role.USER, Role.ADMIN));
        Long todoId = createTodo(adminToken);

        mockMvc.perform(delete("/api/todos/{id}", todoId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("کاربر عادی اجازه دیدن لیست کاربران را ندارد")
    void user_cannotListUsers_returnsForbidden() throws Exception {
        String token = login(userName, EnumSet.of(Role.USER));

        mockMvc.perform(get("/api/users").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("مدیر می‌تواند لیست کاربران را ببیند و نقش‌ها را تغییر دهد")
    void admin_canListAndUpdateUsers() throws Exception {
        String adminToken = login(adminName, EnumSet.of(Role.USER, Role.ADMIN));
        createUser(userName, EnumSet.of(Role.USER));

        mockMvc.perform(get("/api/users").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        Long targetId = userRepository.findByUsername(userName).orElseThrow().getId();

        mockMvc.perform(put("/api/users/{id}/roles", targetId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\":[\"USER\",\"ADMIN\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetId));
    }

    @Test
    @DisplayName("پروفایل کاربر جاری از روی توکن خوانده می‌شود")
    void me_returnsCurrentProfile() throws Exception {
        String token = login(userName, EnumSet.of(Role.USER));

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(userName));
    }

    @Test
    @DisplayName("پیشوند Bearer با هر حروف بزرگ/کوچک پذیرفته می‌شود")
    void lowercaseBearerScheme_isAccepted() throws Exception {
        String token = login(userName, EnumSet.of(Role.USER));

        mockMvc.perform(get("/api/todos").header("Authorization", "bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("پیشوند تکراری Bearer که در Swagger رخ می‌دهد نادیده گرفته می‌شود")
    void duplicatedBearerScheme_isAccepted() throws Exception {
        String token = login(userName, EnumSet.of(Role.USER));

        mockMvc.perform(get("/api/todos").header("Authorization", "Bearer Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("توکن بدون پیشوند Bearer طبق RFC 6750 پذیرفته نمی‌شود")
    void tokenWithoutBearerScheme_isRejected() throws Exception {
        String token = login(userName, EnumSet.of(Role.USER));

        mockMvc.perform(get("/api/todos").header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("توکن منقضی‌شده رد می‌شود")
    void request_withExpiredToken_returnsUnauthorized() throws Exception {
        login(userName, EnumSet.of(Role.USER));

        mockMvc.perform(get("/api/todos").header("Authorization", bearer(expiredTokenFor(userName))))
                .andExpect(status().isUnauthorized());
    }

    private String expiredTokenFor(String username) {
        return Jwts.builder()
                .subject(username)
                .claim(JwtService.ROLES_CLAIM, List.of(Role.USER.name()))
                .issuedAt(Date.from(Instant.now().minusSeconds(7200)))
                .expiration(Date.from(Instant.now().minusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret)))
                .compact();
    }

    private String login(String username, EnumSet<Role> roles) throws Exception {
        createUser(username, roles);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCredentials(username, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private Long createTodo(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"کار موقت\",\"completed\":false}"))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void createUser(String username, EnumSet<Role> roles) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRoles(roles);
        user.setEnabled(Boolean.TRUE);
        userRepository.save(user);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private static Credentials newCredentials(String username, String password) {
        return new Credentials(username, password);
    }

    private record Credentials(String username, String password) {
    }
}
