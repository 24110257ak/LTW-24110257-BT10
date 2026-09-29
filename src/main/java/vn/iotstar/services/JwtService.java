package vn.iotstar.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtService - Viết lại bằng Nimbus JOSE + JWT 9.37.3
 *
 * Nimbus API:
 *  - Tạo token : JWSHeader + JWTClaimsSet + SignedJWT + MACSigner (HS256)
 *  - Xác thực  : SignedJWT.verify(MACVerifier)
 *  - Đọc claims: SignedJWT.getJWTClaimsSet()
 *
 * Không dùng JJWT, không dùng Jwts.builder() / Jwts.parser()
 */
@Service
public class JwtService {

    /**
     * Secret key in Hex format (256-bit) từ application.properties
     */
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    /**
     * Thời hạn token (milliseconds): 3600000 = 1 giờ
     */
    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    // ============================================================
    // Public API
    // ============================================================

    /**
     * Sinh JWT token cho UserDetails (không extra claims)
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Sinh JWT token kèm extra claims tùy chỉnh
     *
     * @param extraClaims map of additional claims to embed in payload
     * @param userDetails the authenticated user
     * @return signed JWT string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        try {
            // 1. Tạo JWSHeader với thuật toán HS256
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            // 2. Xây dựng JWTClaimsSet (payload)
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())             // sub = email
                    .issueTime(new Date())                          // iat = now
                    .expirationTime(new Date(                       // exp = now + expiration
                            System.currentTimeMillis() + jwtExpiration));

            // Thêm extra claims
            for (Map.Entry<String, Object> entry : extraClaims.entrySet()) {
                claimsBuilder.claim(entry.getKey(), entry.getValue());
            }

            JWTClaimsSet claimsSet = claimsBuilder.build();

            // 3. Tạo SignedJWT từ header và claims
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);

            // 4. Ký bằng MACSigner (HMAC-SHA256) với secret key bytes
            MACSigner signer = new MACSigner(hexStringToByteArray(secretKey));
            signedJWT.sign(signer);

            // 5. Serialize thành chuỗi compact "eyJ...header.payload.signature"
            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException("Lỗi khi tạo JWT token bằng Nimbus: " + e.getMessage(), e);
        }
    }

    /**
     * Trích xuất username (subject) từ JWT token
     *
     * @param token chuỗi JWT
     * @return email của user
     */
    public String extractUsername(String token) {
        try {
            return parseSignedJWT(token).getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Lỗi parse JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra token có hợp lệ không (chữ ký đúng + chưa hết hạn + đúng user)
     *
     * @param token       chuỗi JWT cần kiểm tra
     * @param userDetails thông tin user cần so khớp
     * @return true nếu token hợp lệ
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            SignedJWT signedJWT = parseSignedJWT(token);

            // 1. Xác thực chữ ký bằng MACVerifier
            MACVerifier verifier = new MACVerifier(hexStringToByteArray(secretKey));
            boolean signatureValid = signedJWT.verify(verifier);

            if (!signatureValid) {
                return false;
            }

            // 2. Kiểm tra username khớp
            String subject = signedJWT.getJWTClaimsSet().getSubject();
            if (!subject.equals(userDetails.getUsername())) {
                return false;
            }

            // 3. Kiểm tra token chưa hết hạn
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expiration != null && expiration.after(new Date());

        } catch (ParseException | JOSEException e) {
            // Token lỗi cú pháp hoặc chữ ký sai → không hợp lệ
            return false;
        }
    }

    /**
     * Lấy thời hạn token (milliseconds) — dùng cho LoginResponse
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }

    // ============================================================
    // Private Helpers
    // ============================================================

    /**
     * Parse chuỗi JWT thành SignedJWT object của Nimbus
     *
     * @param token chuỗi JWT
     * @return SignedJWT đã parse
     * @throws ParseException nếu chuỗi không đúng định dạng JWT
     */
    private SignedJWT parseSignedJWT(String token) throws ParseException {
        return SignedJWT.parse(token);
    }

    /**
     * Chuyển chuỗi Hex sang mảng byte[]
     * Ví dụ: "4A61..." → new byte[]{0x4A, 0x61, ...}
     *
     * @param hex chuỗi Hex hợp lệ (số ký tự chẵn)
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
