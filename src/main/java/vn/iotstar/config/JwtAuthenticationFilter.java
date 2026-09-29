package vn.iotstar.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import vn.iotstar.services.JwtService;

import java.io.IOException;

/**
 * JwtAuthenticationFilter - Intercepts every request ONCE and validates the JWT token.
 *
 * Flow:
 *  1. Extract Bearer token from Authorization header
 *  2. Extract username (email) from token
 *  3. Load UserDetails from database
 *  4. Validate token
 *  5. Set Authentication into SecurityContextHolder
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Read Authorization header
        final String authHeader = request.getHeader("Authorization");

        // 2. If no Bearer token, skip this filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Extract the token (remove "Bearer " prefix)
            final String jwt = authHeader.substring(7);

            // 4. Extract username from the token
            final String userEmail = jwtService.extractUsername(jwt);

            // 5. Validate and set authentication if not already set
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Load user from database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 7. Validate the token
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // 8. Build authentication object
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // 9. Set additional request details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 10. Update SecurityContextHolder
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // 11. Continue filter chain
            filterChain.doFilter(request, response);

        } catch (Exception exception) {
            // Delegate exception handling to Spring's exception resolver
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }
}
