package com.intuit.socialmedia.posts.auth;

import com.intuit.socialmedia.posts.repository.UserDao;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Component
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDao userDao;
    private final JwtUtil jwtUtil;

    @Autowired
    public JwtRequestFilter(UserDao userDao, JwtUtil jwtUtil) {
        this.userDao = userDao;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (isUnsecuredEndpoint(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String jwt = extractJwtFromRequest(request);
        if (jwt == null) {
            throw new BadCredentialsException("Invalid JWT token");
        }

        String username = validateJwtAndGetUsername(jwt);

        // Authenticate user and set authentication in the security context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            com.intuit.socialmedia.posts.entity.User user = userDao.findByEmail(username).orElse(null);
            if (user != null) {
                // Create an instance of CustomUserDetails
                CustomUserDetails userDetails = new CustomUserDetails(user.getId(), user.getName(), user.getEmail(), "", List.of());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }

    private String validateJwtAndGetUsername(String jwt) {
        try {
            Claims claims = jwtUtil.validateToken(jwt, jwtUtil.publicKey());
            return claims.getSubject();
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid JWT token");
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    public static final String[] WHITE_LIST_URLS = {
            "v1/user/login",
            "v1/user/register",
            "/v3/api-docs",
            "/swagger-ui/",
            "/swagger-resources/"
    };

    private boolean isUnsecuredEndpoint(String requestURI) {
        return Arrays.stream(WHITE_LIST_URLS)
                .anyMatch(requestURI::contains);
    }
}
