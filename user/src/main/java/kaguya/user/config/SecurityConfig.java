package kaguya.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // 세션 방식을 사용 안해서 활성화 할 필요가 없음 (JWT 토큰 방식은 csrf 공격 공격에 무관)
                .formLogin(AbstractHttpConfigurer::disable)  // 프론트가 따로 있으니 폼로그인 활성화할 필요 없음 (CSR)
                .httpBasic(AbstractHttpConfigurer::disable)  // Basic 방식 사용하지 않아서 비활성화 (Bearer 방식 사용)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 세션을 활성화하지 않음 (JWT 사용)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());  // 모든 요청을 허락 (gateway에서 검증 할거니까 들어오는 요청은 인증된 유저)

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
