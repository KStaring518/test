package com.example.shop.config;

import com.example.shop.security.JwtAuthenticationFilter;
import com.example.shop.security.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
            .authorizeRequests()
                // 健康检查 - 无需认证
                .antMatchers("/health", "/").permitAll()
                // H2 控制台 - 开发环境使用
                .antMatchers("/h2-console/**").permitAll()
                // 公开接口 - 无需认证
                .antMatchers("/products/public/**", "/categories/public/**", "/banners/public/**").permitAll()
                // 前端路由刷新（仅GET）
                .antMatchers(HttpMethod.GET, "/products", "/product/**").permitAll()
                // 认证接口 - 无需认证
                .antMatchers("/auth/login", "/auth/register", "/auth/users").permitAll()
                // 商家注册申请：允许已登录或未登录的预检请求，通过JWT拦截器会自动判断，无token会返回401由前端处理
                .antMatchers("/auth/merchant/register").authenticated()
                // 公开测试接口 - 无需认证
                .antMatchers("/public/**").permitAll()
                // 基本测试接口 - 无需认证
                .antMatchers("/basic-test").permitAll()
                // 聊天接口 - 临时允许无需认证（用于测试）
                .antMatchers("/chat/**").permitAll()
                // 测试接口 - 临时允许无需认证（用于测试）
                .antMatchers("/test/**").permitAll()
                // 管理员接口 - 需要ADMIN角色
                .antMatchers("/admin/**").hasRole("ADMIN")
                // 商家接口 - 需要MERCHANT角色
                .antMatchers("/merchant/**").hasRole("MERCHANT")
                // 用户接口 - USER 或 MERCHANT 都可访问（商家也有个人中心）
                .antMatchers("/user/**").hasAnyRole("USER", "MERCHANT")
                .antMatchers("/cart/**").hasAnyRole("USER", "MERCHANT")
                .antMatchers("/order/**").hasAnyRole("USER", "MERCHANT")
                // 物流接口 - 需要USER或MERCHANT角色
                .antMatchers("/logistics/**").hasAnyRole("USER", "MERCHANT")
                .antMatchers("/uploads/**").permitAll()
                // 支付宝创建支付页、回调与同步回跳放行（同时放行带/api前缀的反向代理路径）
                .antMatchers(
                        "/alipay/create-page",
                        "/alipay/notify",
                        "/alipay/return",
                        "/alipay/redirect",
                        "/api/alipay/create-page",
                        "/api/alipay/notify",
                        "/api/alipay/return",
                        "/api/alipay/redirect"
                ).permitAll()
                // 收藏接口需要登录
                .antMatchers("/favorite/**").authenticated()
                // 其他接口需要认证
                .anyRequest().authenticated()
            .and()
            .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 只对需要认证的路径应用JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
