package kaguya.domain.user.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${custom.jwt.secrets}")
    private String secret;

    private final long EXP_ACCESS_TOKEN = 1000L * 60 * 10; // 10분 : 60(1분) * 10
    private final long EXP_REFRESH_TOKEN = 1000L * 60 * 60 * 24 * 14; // 14일 : 60(1분) * 60(1시간) * 24(1일) * 14(14일)

    /**
     * AccessToken 생성 (접근)
     * @param username: 토큰 식별자
     */
    public String createAccessToken(String username) {
        return issueToken(username, EXP_ACCESS_TOKEN);
    }

    /**
     * RefreshToken 생성 (갱신)
     * @param id: 토큰 식별자
     */
    public String createRefreshToken(String id) {
        return issueToken(id, EXP_REFRESH_TOKEN);
    }

    /**
     * 토큰이 유효한지 검증
     * @param token: RefreshToken
     */
    public boolean validationToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 토큰으로 아이디 조회
     * @param token: RefreshToken
     */
    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject(); // 생성할 때 Subject()로 넣었던 값 (username)
    }

    /**
     * 토큰 생성 로직
     * @param username: 토큰 식별자 (id)
     * @param expTime: 토큰 만료일
     */
    private String issueToken(String username, long expTime) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(username)  // 식별자
                .issuedAt(new Date(now))  // 발급일
                .expiration(new Date(now + expTime))  // 만료일
                .signWith(getSecretKey())  // 서명
                .compact();
    }
/*
    // compact() 의미
    for (Map.Entry<String, Object> claimEntry : claims.entrySet()) {
        builder.claim(claimEntry.getKey(), claimEntry.getValue());
    }
*/

    /**
     * String 타입 -> SecretKey 타입
     * SecretKey: HMAC-SHA 알고리즘(해시)으로 만든 서명키
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
