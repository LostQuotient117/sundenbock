package de.nak.iaa.sundenbock.config;

import de.nak.iaa.sundenbock.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Ein Filter, der bei jeder Anfrage ausgeführt wird, um JWT-Token zu validieren.
 * Er fängt Anfragen ab, extrahiert den JWT aus dem Authorization-Header, validiert ihn
 * und setzt die Benutzerauthentifizierung im SecurityContextHolder, wenn der Token gültig ist.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Führt die Filterlogik aus. Prüft auf einen "Bearer"-Token im "Authorization"-Header.
     * Wenn ein gültiger Token gefunden wird, wird der Benutzer authentifiziert und der SecurityContext aktualisiert.
     *
     * @param request Die eingehende HTTP-Anfrage.
     * @param response Die ausgehende HTTP-Antwort.
     * @param filterChain Die Filterkette, um die Anfrage an den nächsten Filter weiterzuleiten.
     * @throws ServletException wenn ein Servlet-Fehler auftritt.
     * @throws IOException wenn ein I/O-Fehler auftritt.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String jwt = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwt);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
