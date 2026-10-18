package kaguya.user.domain.auth.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${custom.jwt.secrets}")
    private String secret;

    private final long EXP_ACCESS_TOKEN = 1000L * 60 * 10;  // 10분 : 60(1분) * 10
    private final long EXP_REFRESH_TOKEN = 1000L * 60 * 60 * 24 * 14;  // 14일 : 60(1분) * 60(1시간) * 24(1일) * 14(14일)

    /**
     * AccessToken 생성 (접근)
     * @param username: 토큰 식별자
     */
    public String createAccessToken(String username, String role) {
        return issueAccessToken(username, role, EXP_ACCESS_TOKEN);
    }

    /**
     * RefreshToken 생성 (갱신)
     * @param username: 토큰 식별자
     */
    public String createRefreshToken(String username) {
        return issueRefreshToke(username, EXP_REFRESH_TOKEN);
    }


    /**
     * Access Token 검증
     * @param token: AccessToken
     */
    public void validateAccessToken(String token) {
        validateToken(token, ErrorCode.EXPIRED_ACCESS_TOKEN);
    }

    /**
     * Refresh Token 검증
     * @param token: RefreshToken
     */
    public void validateRefreshToken(String token) {
        validateToken(token, ErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    /**
     * 토큰이 유효한지 검증
     * @param token: AccessToken / RefreshToken
     * @param error: 토큰 만료 시 던질 에러코드
     */
    private void validateToken(String token, ErrorCode error) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(error);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰으로 아이디 조회
     * @param token: AccessToken/RefreshToken
     */
    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();  // 생성할 때 Subject()로 넣었던 값 (username)
    }

    /**
     * 토큰으로 권한 조회
     * @param token: AccessToken
     */
    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);  // 생성할 때 claim()로 넣었던 값(role)
    }

    /**
     * 토큰 생성 로직
     * @param username: 토큰 식별자 (id)
     * @param role: 페이로드 (role)
     * @param expTime: 토큰 만료일
     */
    private String issueAccessToken(String username, String role, long expTime) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(username)  // 식별자(아이디)
                .claim("role", role)  // 페이로드(권한)
                .issuedAt(new Date(now))  // 발급일
                .expiration(new Date(now + expTime))  // 만료일
                .signWith(getSecretKey())  // 서명
                .compact();
    }

    private String issueRefreshToke(String username, long expTime) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(username)  // 식별자(아이디)
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
