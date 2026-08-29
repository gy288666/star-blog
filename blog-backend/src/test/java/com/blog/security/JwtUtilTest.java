package com.blog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** JWT 签发/解析/防篡改测试。 */
class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil(
            "unit-test-secret-key-with-at-least-32-characters!!", 3_600_000);

    @Test
    void generateAndParse() {
        String token = jwtUtil.generate(1L, "admin", "admin");
        Claims claims = jwtUtil.parse(token);
        assertEquals("admin", claims.getSubject());
        assertEquals(1L, claims.get("id", Long.class));
        assertEquals("admin", claims.get("role", String.class));
    }

    @Test
    void tamperedTokenRejected() {
        String token = jwtUtil.generate(1L, "admin", "admin");
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertThrows(JwtException.class, () -> jwtUtil.parse(tampered));
    }

    @Test
    void garbageTokenRejected() {
        assertThrows(Exception.class, () -> jwtUtil.parse("not-a-jwt"));
    }
}
