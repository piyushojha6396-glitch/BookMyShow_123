package com.cfs.BMS.security;

import com.cfs.BMS.entity.User;
import com.cfs.BMS.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Reads "Authorization: Bearer <jwt>", validates it and re-checks the user in the database
 * so that deactivated accounts and role changes take effect immediately.
 * Invalid tokens are simply ignored - the entry point then answers 401 for protected routes.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(PREFIX.length()).trim();
            Optional<Long> userId = jwtService.extractUserId(token);
            if (userId.isPresent()) {
                Optional<User> user = userRepository.findById(userId.get()).filter(User::isActive);
                user.ifPresent(u -> {
                    AuthenticatedUser principal = new AuthenticatedUser(u.getId(), u.getEmail(), u.getRole());
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }
        chain.doFilter(request, response);
    }
}
