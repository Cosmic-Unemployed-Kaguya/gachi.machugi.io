package kaguya.domain.user.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnvoyHeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Envoy가 넘겨준 헤더
        String username = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");

        // Envoy가 아이디 넘겨주면 (유효한 인증 요청으로 간주)
        if (username != null) {

            // 인가(Authorization) 처리에 사용할 권한 리스트
            List<GrantedAuthority> authorities = new ArrayList<>();

            if (userRole != null) {
                // 권한 설정하고 리스트에 추가
                authorities.add(new SimpleGrantedAuthority(userRole));
            }

            // 인증된 사용자 정보를 담은 객체
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);  // 인증된 사용자이므로 credentials(password)는 null

            // HTTP 요청에 대한 메타데이터(IP, Session ID) WebAuthenticationDetails로 생성하여 설정 (로그용)
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // SecurityContextHolder에 객체 저장해서 이후 요청을 '인증된 사용자'로 인식 하도록
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 다음 필터로 이동
        filterChain.doFilter(request, response);
    }
}
