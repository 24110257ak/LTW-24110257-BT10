package vn.iotstar.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtService - Handles JWT token generation and validation using JJWT 0.12.6
 *
 * Sử dụng thuật toán HS256 (HMAC-SHA256) với khóa bí mật dạng Hex.
 */
@Service
public class JwtService {

    /**
     * Secret key in Hex format (256-bit) loaded from application.properties
     */
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    /**
     * Token expiration time in milliseconds (e.g., 3600000 = 1 hour)
     */
    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    // ============================================================
    // Public API
    // ============================================================

    /**
     * Generate a JWT token for the given UserDetails (no extra claims)
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generate a JWT token with extra claims
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Get the expiration time value (milliseconds)
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }

    /**
     * Extract the username (subject) from a JWT token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validate a JWT token against the given UserDetails
     *
     * @param token       the JWT token string
     * @param userDetails the expected user
     * @return true if token is valid and not expired
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // ============================================================
    // Private Helpers
    // ============================================================

    /**
     * Build the JWT token using JJWT 0.12.6 API
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts.builder()
                .claims(extraClaims)                              // extra claims first
                .subject(userDetails.getUsername())               // set subject = email
                .issuedAt(new Date(System.currentTimeMillis()))   // issued now
                .expiration(new Date(System.currentTimeMillis() + expiration)) // expires in
                .signWith(getSigningKey())                        // sign with HS256 key
                .compact();                                       // produce compact string
    }

    /**
     * Check whether a token has expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract the expiration date from the token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claim extractor using a claims resolver function
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse and return all claims from the JWT token using JJWT 0.12.6 API
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())    // set the signing key for verification
                .build()
                .parseSignedClaims(token)       // parse the signed JWT (new API in 0.12.x)
                .getPayload();                  // get claims payload
    }

    /**
     * Build the SecretKey from the Hex-encoded secret string.
     * Keys.hmacShaKeyFor() accepts raw bytes; we decode the Hex string first.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = hexStringToByteArray(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Convert a Hex string to a byte array
     *
     * @param hex a valid Hex string (even number of characters)
     * @return byte array
     */
    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
