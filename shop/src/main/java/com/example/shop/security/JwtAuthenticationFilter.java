package com.example.shop.security;

import com.example.shop.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Collections;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // 不再注入 UserDetailsService，直接从 JWT 中解析角色

    @Override
    protected boolean shouldNotFilter(@org.springframework.lang.NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // 跳过不需要认证的路径
        boolean isAuthPath = path.startsWith("/auth/");
        boolean isMerchantApply = "/auth/merchant/register".equals(path);

        return path.startsWith("/chat/") || 
               path.startsWith("/test/") || 
               // 登录/注册等 /auth/* 默认跳过，但商家申请需要鉴权，不能跳过
               (isAuthPath && !isMerchantApply) || 
               path.startsWith("/uploads/") || 
               path.startsWith("/products/public/") ||
               path.startsWith("/categories/public/") ||
               path.startsWith("/banners/public/") ||
               path.startsWith("/alipay/") ||
               path.startsWith("/public/") ||
               path.equals("/health") || 
               path.equals("/") ||
               path.equals("/basic-test");
    }

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request, @org.springframework.lang.NonNull HttpServletResponse response, @org.springframework.lang.NonNull FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 从JWT中提取角色信息，而不是重新从数据库加载
            String role = jwtUtil.extractRole(jwt);
            if (role != null && jwtUtil.validateToken(jwt, username)) {
                // 创建权限列表
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role)
                );
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(request, response);
    }
}
