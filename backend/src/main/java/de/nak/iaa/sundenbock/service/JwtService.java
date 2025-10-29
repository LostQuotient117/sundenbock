package de.nak.iaa.sundenbock.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

/**
 * Ein Service für die Verwaltung von JSON Web Tokens.
 * Verantwortlich für die Erstellung, das Parsen und die Validierung von Tokens.
 */
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extrahiert den Benutzernamen (Subject) aus dem JWT-Token.
     *
     * @param token Der JWT-Token.
     * @return Der Benutzername.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrahiert einen bestimmten Claim aus dem JWT-Token mithilfe einer Resolver-Funktion.
     *
     * @param token Der JWT-Token.
     * @param claimsResolver Eine Funktion, die den gewünschten Claim aus den Claims extrahiert.
     * @param <T> Der Typ des Claims.
     * @return Der extrahierte Claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generiert einen neuen JWT-Token für die angegebenen UserDetails.
     *
     * @param userDetails Die UserDetails des Benutzers.
     * @return Der generierte JWT-Token als String.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generiert einen neuen JWT-Token mit zusätzlichen Claims für die angegebenen UserDetails.
     *
     * @param extraClaims Zusätzliche Claims, die dem Token hinzugefügt werden sollen.
     * @param userDetails Die UserDetails des Benutzers.
     * @return Der generierte JWT-Token als String.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Überprüft, ob ein JWT-Token gültig ist.
     * Ein Token ist gültig, wenn der Benutzername im Token mit dem der UserDetails übereinstimmt
     * und der Token nicht abgelaufen ist.
     *
     * @param token Der zu validierende JWT-Token.
     * @param userDetails Die UserDetails zum Vergleich.
     * @return true, wenn der Token gültig ist, andernfalls false.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
