package kaguya.user.domain.auth.config;

import kaguya.user.domain.auth.config.filter.ExtAuthzFilter;
import kaguya.user.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthService authService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)  // 세션 방식을 사용 안해서 활성화 할 필요가 없음 (JWT 토큰 방식은 csrf 공격 공격에 무관)
                .formLogin(AbstractHttpConfigurer::disable)  // 프론트가 따로 있으니 폼로그인 활성화할 필요 없음 (CSR)
                .httpBasic(AbstractHttpConfigurer::disable)  // Basic 방식 사용하지 않아서 비활성화 (Bearer 방식 사용)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 세션을 활성화하지 않음 (JWT 사용)

                // 페이지 접근 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()  // 인증 관련은 전부 통과
                        .anyRequest().authenticated()  // 나머지는 Envoy가 인증해준 유저만
                )

                // ExtAuthzFilter(): Envoy의 ext_authz 필터 설정
                // UsernamePasswordAuthenticationFilter: 실행 위치(순서)를 잡기 위한 앵커 용도 (실제 필터가 적용되진 않음)
                .addFilterBefore(new ExtAuthzFilter(authService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}