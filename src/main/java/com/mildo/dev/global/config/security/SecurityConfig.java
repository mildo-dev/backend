package com.mildo.dev.global.config.security;

import com.mildo.dev.api.member.customoauth.handler.CustomLogoutSuccessHandler;
import com.mildo.dev.api.member.customoauth.handler.CustomOAuthFailureHandler;
import com.mildo.dev.api.member.customoauth.handler.CustomOAuthUserService;
import com.mildo.dev.api.member.repository.MemberRepository;
import com.mildo.dev.api.member.repository.TokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final MemberRepository userRepository;
    private final TokenRepository tokenRepository;
    private final CorsFilter corsFilter;

    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

//    public SecurityConfig(MemberRepository userRepository, TokenRepository tokenRepository, CorsFilter corsFilter) {
//        this.userRepository = userRepository;
//        this.tokenRepository = tokenRepository;
//        this.corsFilter = corsFilter;
//    }

    public SecurityConfig(MemberRepository userRepository, TokenRepository tokenRepository, CorsFilter corsFilter, CustomLogoutSuccessHandler customLogoutSuccessHandler) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.corsFilter = corsFilter;
        this.customLogoutSuccessHandler = customLogoutSuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login/oauth2/code/*", "/oauth2/authorization/*").permitAll()  // OAuth2 리디렉션 URL 허용
                        .anyRequest().permitAll() // 모든 요청 허용
                );

        http
                .csrf((auth) -> auth.disable()); // CSRF 비활성화

        http
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(customLogoutSuccessHandler)
//                        .logoutSuccessHandler(new CustomLogoutSuccessHandler(tokenRepository))
                        .logoutSuccessUrl("/") // 로그아웃 후 리디렉션
                        .invalidateHttpSession(true) // 세션 무효화
                        .deleteCookies("JSESSIONID", "RefreshToken") // JSESSIONID 쿠키 삭제
                );

        http
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuthUserService())
                        )
                        .defaultSuccessUrl("/loginSuccess", true) // 로그인 성공 시 리디렉션
                        .failureHandler(new CustomOAuthFailureHandler()) // 로그인 실패 시 리디렉션
                );

        http
                .addFilter(corsFilter); // CORS

        return http.build();
    }

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuthUserService() {
        return new CustomOAuthUserService(userRepository);
    }

}
