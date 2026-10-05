package com.example.todo.security;

import com.example.todo.entity.AppUser;
import com.example.todo.entity.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "dGVzdC1zZWNyZXQtd2l0aC1hdC1sZWFzdC0zMi1ieXRlcy1sb25nISE=";
    private static final long ONE_HOUR = 3_600_000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, ONE_HOUR);
    }

    @Test
    @DisplayName("توکن صادرشده حاوی نام کاربری و نقش‌هاست")
    void generateToken_embedsUsernameAndRoles() {
        String token = jwtService.generateToken(user("sam", Role.USER, Role.ADMIN));

        assertThat(jwtService.extractUsername(token)).isEqualTo("sam");
        assertThat(jwtService.extractRoles(token)).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    @DisplayName("توکن معتبر برای همان کاربر تأیید می‌شود")
    void isValid_trueForMatchingUsername() {
        String token = jwtService.generateToken(user("sam", Role.USER));

        assertThat(jwtService.isValid(token, "sam")).isTrue();
    }

    @Test
    @DisplayName("توکن برای کاربر دیگر معتبر نیست")
    void isValid_falseForDifferentUsername() {
        String token = jwtService.generateToken(user("sam", Role.USER));

        assertThat(jwtService.isValid(token, "ali")).isFalse();
    }

    @Test
    @DisplayName("توکن منقضی‌شده نامعتبر است")
    void isValid_falseForExpiredToken() {
        JwtService expiredService = new JwtService(SECRET, -ONE_HOUR);
        String token = expiredService.generateToken(user("sam", Role.USER));

        assertThat(jwtService.isValid(token, "sam")).isFalse();
    }

    @Test
    @DisplayName("توکن دستکاری‌شده با امضای نامعتبر رد می‌شود")
    void isValid_falseForTamperedSignature() {
        String token = jwtService.generateToken(user("sam", Role.USER));

        assertThat(jwtService.isValid(token + "tampered", "sam")).isFalse();
    }

    @Test
    @DisplayName("توکن با کلید دیگر نامعتبر است")
    void isValid_falseWhenSignedWithDifferentSecret() {
        JwtService otherService = new JwtService(
                "YW5vdGhlci1zZWNyZXQtd2l0aC1hdC1sZWFzdC0zMi1ieXRlcy1sb25nISE=", ONE_HOUR);

        String token = otherService.generateToken(user("sam", Role.USER));

        assertThat(jwtService.isValid(token, "sam")).isFalse();
    }

    @Test
    @DisplayName("توکن با ساختار نامعتبر کنترل می‌شود")
    void isValid_falseForMalformedToken() {
        assertThat(jwtService.isValid("not-a-jwt", "sam")).isFalse();
    }

    @Test
    @DisplayName("توکن فاقد نقش، مجموعه خالی برمی‌گرداند")
    void extractRoles_emptyWhenClaimAbsent() {
        String tokenWithoutRoles = Jwts.builder()
                .subject("sam")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ONE_HOUR))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET)))
                .compact();

        Set<String> roles = jwtService.extractRoles(tokenWithoutRoles);

        assertThat(roles).isEmpty();
    }

    private AppUser user(String username, Role... roles) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setRoles(roles.length == 0 ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(List.of(roles)));
        return user;
    }
}
