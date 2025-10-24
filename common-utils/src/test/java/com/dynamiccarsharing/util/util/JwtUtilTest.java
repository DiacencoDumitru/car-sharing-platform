package com.dynamiccarsharing.util.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = JwtUtil.class)
@TestPropertySource(properties = {
    "application.security.jwt.secret-key=NDM0YjVmNjM1MTY0NWE3NDc3Mzg3YTMzNjE1MTQxNTg0NTUxNDg0ZjU0N2IzYjRmNmE1YjdhNmE0ZDQxNjM1Mg=="
})
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private final String TEST_SECRET_KEY = "NDM0YjVmNjM1MTY0NWE3NDc3Mzg3YTMzNjE1MTQxNTg0NTUxNDg0ZjU0N2IzYjRmNmE1YjdhNmE0ZDQxNjM1Mg==";
    private final long EXPIRATION_MS = 3600000;

    private String validToken;
    private String expiredToken;
    private String tokenWithUserId;
    private String tokenWithAuthorities;
    private String tokenWithNonNumericUserId;
    private String tokenWithNonListAuthorities;
    private String tokenInvalidSignature;
    private String tokenMalformed;
    private String tokenUnsupported;

    @BeforeEach
    void setUp() {
        Map<String, Object> claims = new HashMap<>();
        validToken = generateToken(claims, "testuser", EXPIRATION_MS);

        claims.clear();
        expiredToken = generateToken(claims, "expireduser", -EXPIRATION_MS);

        claims.clear();
        claims.put("userId", 123L);
        tokenWithUserId = generateToken(claims, "userWithId", EXPIRATION_MS);

        claims.clear();
        claims.put("authorities", List.of("ROLE_USER", "ROLE_ADMIN"));
        tokenWithAuthorities = generateToken(claims, "userWithRoles", EXPIRATION_MS);

        claims.clear();
        claims.put("userId", "not-a-number");
        tokenWithNonNumericUserId = generateToken(claims, "userBadId", EXPIRATION_MS);

        claims.clear();
        claims.put("authorities", "ROLE_USER");
         tokenWithNonListAuthorities = generateToken(claims, "userBadAuths", EXPIRATION_MS);

        tokenInvalidSignature = validToken.substring(0, validToken.length() - 1) + "X";
        tokenMalformed = "this.is.not.a.jwt";
    }

    private String generateToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(TEST_SECRET_KEY), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    @DisplayName("extractUsername extracts correct subject")
    void extractUsername_validToken_returnsSubject() {
        assertEquals("testuser", jwtUtil.extractUsername(validToken));
    }

    @Test
    @DisplayName("extractUserId extracts correct Long userId")
    void extractUserId_validTokenWithLongId_returnsLong() {
        assertEquals(123L, jwtUtil.extractUserId(tokenWithUserId));
    }

    @Test
    @DisplayName("extractUserId returns null if userId claim is missing")
    void extractUserId_tokenWithoutId_returnsNull() {
        assertNull(jwtUtil.extractUserId(validToken));
    }

     @Test
    @DisplayName("extractUserId returns null if userId claim is not a number")
    void extractUserId_tokenWithNonNumericId_returnsNull() {
        assertNull(jwtUtil.extractUserId(tokenWithNonNumericUserId));
    }

    @Test
    @DisplayName("extractUserId handles userId as String number")
    void extractUserId_tokenWithStringNumericId_returnsLong() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "456");
        String token = generateToken(claims, "userStringId", EXPIRATION_MS);
        assertEquals(456L, jwtUtil.extractUserId(token));
    }

    @Test
    @DisplayName("extractAuthorities extracts correct list of strings")
    void extractAuthorities_validTokenWithList_returnsList() {
        List<String> authorities = jwtUtil.extractAuthorities(tokenWithAuthorities);
        assertEquals(List.of("ROLE_USER", "ROLE_ADMIN"), authorities);
    }

     @Test
    @DisplayName("extractAuthorities returns empty list if authorities claim is missing")
    void extractAuthorities_tokenWithoutAuthorities_returnsEmptyList() {
        List<String> authorities = jwtUtil.extractAuthorities(validToken);
        assertTrue(authorities.isEmpty());
    }

    @Test
    @DisplayName("extractAuthorities returns empty list if authorities claim is not a list")
    void extractAuthorities_tokenWithNonListAuthorities_returnsEmptyList() {
        List<String> authorities = jwtUtil.extractAuthorities(tokenWithNonListAuthorities);
        assertTrue(authorities.isEmpty());
    }

     @Test
    @DisplayName("extractAuthorities handles list with null values")
    void extractAuthorities_tokenWithNullInList_filtersNull() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", Arrays.asList("ROLE_ONE", null, "ROLE_TWO"));
        String token = generateToken(claims, "userNullAuth", EXPIRATION_MS);
        List<String> authorities = jwtUtil.extractAuthorities(token);
         assertEquals(List.of("ROLE_ONE", "ROLE_TWO"), authorities);
    }


    @Test
    @DisplayName("extractClaim extracts specific claim using resolver")
    void extractClaim_validToken_returnsClaim() {
        assertNull(jwtUtil.extractClaim(validToken, Claims::getIssuer));
        assertEquals("testuser", jwtUtil.extractClaim(validToken, Claims::getSubject));
    }

    @Test
    @DisplayName("isTokenValid returns true for valid token")
    void isTokenValid_validToken_returnsTrue() {
        assertTrue(jwtUtil.isTokenValid(validToken));
    }

    @Test
    @DisplayName("isTokenValid returns false for expired token")
    void isTokenValid_expiredToken_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid(expiredToken));
    }

    @Test
    @DisplayName("isTokenValid returns false for token with null expiration")
    void isTokenValid_nullExpiration_returnsFalse() {
        String tokenNoExp = Jwts.builder()
                               .setSubject("noexp")
                               .setIssuedAt(new Date(System.currentTimeMillis()))
                               .signWith(getSigningKey(TEST_SECRET_KEY), SignatureAlgorithm.HS256)
                               .compact();
        assertFalse(jwtUtil.isTokenValid(tokenNoExp));
    }

    @Test
    @DisplayName("isTokenValid returns false for invalid token signature")
    void isTokenValid_invalidSignature_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid(tokenInvalidSignature));
    }

    @Test
    @DisplayName("isTokenValid returns false for malformed token")
    void isTokenValid_malformedToken_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid(tokenMalformed));
    }

    @Test
    @DisplayName("isTokenValid returns false for null token")
    void isTokenValid_nullToken_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid(null));
    }

    @Test
    @DisplayName("isTokenValid returns false for empty token")
    void isTokenValid_emptyToken_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid(""));
    }
    
    @Test
    @DisplayName("extractClaim throws SignatureException for invalid token signature")
    void extractClaim_invalidSignature_throwsException() {
        assertThrows(SignatureException.class, () -> jwtUtil.extractClaim(tokenInvalidSignature, Claims::getSubject));
    }

    @Test
    @DisplayName("extractClaim throws MalformedJwtException for malformed token")
    void extractClaim_malformedToken_throwsException() {
        assertThrows(MalformedJwtException.class, () -> jwtUtil.extractClaim(tokenMalformed, Claims::getSubject));
    }
}
