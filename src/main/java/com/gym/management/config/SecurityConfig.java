package com.gym.management.config;

import com.gym.management.filter.JwtAuthenticationFilter;
import com.gym.management.handler.CustomAuthenticationEntryPoint; // 如果还没创建这个类，请看下方说明
import com.gym.management.handler.CustomAccessDeniedHandler;     // 如果还没创建这个类，请看下方说明
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 构造函数注入
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 密码编码器
     * 用于注册时加密密码，登录时比对密码
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF (前后端分离 + JWT 不需要 CSRF)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 配置 CORS (允许前端跨域访问)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. 授权规则
                .authorizeHttpRequests(auth -> auth
                        // 放行所有认证相关接口 (登录、注册、刷新令牌等)
                        .requestMatchers("/api/auth/**").permitAll()
                        // 放行 Swagger/OpenAPI 文档 (如果有)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 放行静态资源 (如果有)
                        .requestMatchers("/static/**", "/public/**").permitAll()
                        // 管理员端接口需要 ADMIN 角色
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 会员端接口需要 MEMBER 角色
                        .requestMatchers("/api/member/**").hasRole("MEMBER")
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )

                // 4. 会话管理：无状态 (Stateless)，每次请求都验证 JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 5. 异常处理 (关键！返回 JSON 而不是 HTML 错误页)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()) // 未登录/Token 无效
                        .accessDeniedHandler(new CustomAccessDeniedHandler())           // 权限不足
                )

                // 6. 禁用默认登出 (JWT 不需要服务端登出，由前端移除 Token 即可)
                .logout(AbstractHttpConfigurer::disable)

                // 7. 添加 JWT 过滤器 (在用户名密码过滤器之前执行)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 配置源
     * 允许前端域名访问后端接口
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许的来源 (开发环境)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:3000"
        ));
        // 允许的方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // 允许的头部
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept-Origin"));
        // 是否允许携带凭证 (如果用了 Cookie 需设为 true，纯 JWT 通常 false 即可，但设为 true 更兼容)
        configuration.setAllowCredentials(true);
        // 预检请求缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}