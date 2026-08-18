package com.inventory.officeInventory.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri =
                request.getRequestURI();

        String method =
                request.getMethod();

        String authHeader =
                request.getHeader("Authorization");

        log.info("========================================");
        log.info("JWT FILTER");
        log.info("Request : {} {}", method, requestUri);
        log.info(
                "Authorization header exists : {}",
                authHeader != null
        );

        /*
         * No Authorization header
         */
        if (
                authHeader == null ||
                        !authHeader.startsWith("Bearer ")
        ) {

            log.warn(
                    "No Bearer token for {} {}",
                    method,
                    requestUri
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authHeader.substring(7);

        try {

            /*
             * Extract username
             */
            String username =
                    jwtService.extractUsername(token);

            log.info(
                    "Username from JWT : {}",
                    username
            );

            if (
                    username != null &&
                            SecurityContextHolder
                                    .getContext()
                                    .getAuthentication() == null
            ) {

                /*
                 * Load user from database
                 */
                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(username);

                log.info(
                        "User loaded : {}",
                        userDetails.getUsername()
                );

                log.info(
                        "Authorities : {}",
                        userDetails.getAuthorities()
                );

                /*
                 * Validate token
                 */
                boolean valid =
                        jwtService.isTokenValid(token);

                log.info(
                        "JWT valid : {}",
                        valid
                );

                if (valid) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authentication
                            );

                    Authentication currentAuthentication =
                            SecurityContextHolder
                                    .getContext()
                                    .getAuthentication();

                    log.info(
                            "Authentication set : {}",
                            currentAuthentication
                    );

                    log.info(
                            "Authenticated : {}",
                            currentAuthentication.isAuthenticated()
                    );

                    log.info(
                            "Final authorities : {}",
                            currentAuthentication
                                    .getAuthorities()
                    );

                } else {

                    log.warn(
                            "JWT token is invalid"
                    );
                }

            } else {

                log.warn(
                        "Username is null or SecurityContext already contains authentication"
                );
            }

        } catch (Exception e) {

            /*
             * Very important:
             *
             * Log the actual exception.
             */
            log.error(
                    "JWT authentication failed",
                    e
            );
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}