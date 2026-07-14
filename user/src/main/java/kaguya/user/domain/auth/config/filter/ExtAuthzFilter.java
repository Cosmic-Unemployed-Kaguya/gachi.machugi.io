package kaguya.user.domain.auth.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kaguya.user.domain.auth.model.dto.response.CheckTokenRes;
import kaguya.user.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ExtAuthzFilter extends OncePerRequestFilter {

    private final AuthService authService;

    /**
     * 필터를 적용하지 않을 경로 설정
     * @param request: 현재 HTTP 요청
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth");
    }

    /**
     * Envoy의 ext_authz 토큰 검증 요청을 처리하고 유저 정보를 응답 헤더로 반환
     * @param request: 현재 HTTP 요청
     * @param response: 응답 객체
     * @param filterChain: 필터 객체
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // cookie의 accessToken 값 가져오기
        Cookie cookie = WebUtils.getCookie(request, "accessToken");
        String accessToken = (cookie != null) ? cookie.getValue() : null;

        // accessToken 값이 있으면 인증된 유저
        if (accessToken != null) {
            try {
                CheckTokenRes checkRes = authService.checkToken(accessToken);  // 서비스 로직

                // 유저 정보 헤더에 세팅
                response.setHeader("X-User-Id", checkRes.username());  // 아이디
                response.setHeader("X-User-Role", checkRes.role());  // 권한

                response.setStatus(HttpServletResponse.SC_OK);
                return;

            } catch (Exception e) {
                // 인증 토큰이 맞지 안을 경우 (토큰 불일치, 토큰 만료, 블랙 리스트)
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401 Unauthorized
                return;
            }
        }
        // accessToken 없으면 게스트
        else {
            // X-User-Id와 X-User-Role 빈 값으로 덮기 (헤더 조작 방지)
            response.setHeader("X-User-Id", "");
            response.setHeader("X-User-Role", "");

            // todo. X-Guest-Id 세팅

            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    /**
     * Spring Security Context 구성 (현재 사용 안함)
     * - Controller에서 @AuthenticationPrincipal을 통해 유저 정보(권한)를 주입받을 수 있음
     * - 하지만, 현재 서비스 구조상 Envoy 프록시의 ext_authz 역할만 수행하고,
     * - 각 서비스는 커스텀 인증 헤더(x-user-id, x-user-role 등)을 보고 인증/인가를 확인하기 때문에 이 기능이 필요 없음
     */
    private void setupSecurityContext(HttpServletRequest request, String username, String role) {
        // 인가(Authorization) 처리에 사용할 권한 리스트
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 권한 헤더가 존재하면 Spring Security 권한 객체로 변환하여 추가
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        // 인증된 사용자 정보를 담은 객체 (인증 완료 상태이므로 비밀번호는 null)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        // HTTP 요청에 대한 메타데이터(IP, Session ID) 설정 (로그용)
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // SecurityContext에 객체를 저장하여 이후 로직에서 '인증된 사용자'로 인식하도록 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}