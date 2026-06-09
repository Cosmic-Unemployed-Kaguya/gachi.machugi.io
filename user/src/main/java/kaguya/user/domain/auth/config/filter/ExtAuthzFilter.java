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
     * 필터 적용
     * @param request: 현재 HTTP 요청
     * @param response: 응답 객체
     * @param filterChain: 필터 객체
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 헤더에 아이디가 없음(게스트): Envoy의 토큰 검증 요청
        if (request.getHeader("X-User-Id") == null) {
            verifyTokenForEnvoy(request, response);
            return;
        }

        // 헤더에 아이디가 있음(회원): Spring Security Context 구성
        setupSecurityContext(request, response, filterChain);
    }

    /**
     * [CASE 1] Envoy의 ext_authz 토큰 검증 요청을 처리하고 유저 정보를 응답 헤더로 반환
     * @param request: 현재 HTTP 요청
     * @param response: 응답 객체
     */
    private void verifyTokenForEnvoy(HttpServletRequest request, HttpServletResponse response) {

        // cookie의 accessToken 값 가져오기
        Cookie cookie = WebUtils.getCookie(request, "accessToken");
        String accessToken = (cookie != null) ? cookie.getValue() : null;

        // accessToken이 null이면 게스트 유저
        if (accessToken == null) {
            response.setStatus(HttpServletResponse.SC_OK);  // 200 OK

            // envoy가 인증을 담당 할 경우
            // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // accessToken 값이 있으면 인증된 유저
        try {
            CheckTokenRes checkRes = authService.checkToken(accessToken);  // 서비스 로직

            // 유저 정보 헤더에 세팅
            response.setHeader("X-User-Id", checkRes.username());  // 아이디
            response.setHeader("X-User-Role", checkRes.role());  // 권한

            response.setStatus(HttpServletResponse.SC_OK);  // 200 OK
            return;

        } catch (Exception e) {
            // 인증 토큰이 맞지 안을 경우 (토큰 불일치, 토큰 만료, 블랙 리스트)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401 Unauthorized
            return;
        }
    }

    /**
     * [CASE 2] 헤더의 유저 정보를 바탕으로 Spring Security Context를 구성
     * @param request: 현재 HTTP 요청
     * @param response: 응답 객체
     * @param filterChain: 필터 객체
     */
    private void setupSecurityContext(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 요청 헤더 확인
        String username = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");

        // 헤더에 아이디가 존재하면 Envoy를 통과한 정상적인 인증 유저로 판단
        if (username != null) {

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

            // 다음 필터(컨트롤러)로 요청 전달
            filterChain.doFilter(request, response);
            return;
        }
    }
}