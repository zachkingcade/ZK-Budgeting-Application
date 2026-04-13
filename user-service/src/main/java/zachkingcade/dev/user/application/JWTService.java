package zachkingcade.dev.user.application;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Date;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Component
public class JWTService {
    private final PrivateKey privateKey;

    public JWTService() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        String pem;
        try (InputStream is = new ClassPathResource("keys/private_key.pem").getInputStream()) {
            pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        String privateKeyString = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        privateKey = kf.generatePrivate(spec);
    }

    public String generateUserAccessToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(10 * 60);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("token_type", "user")
                .claim("aud", List.of("ledger-service", "reporting-service"))
                .issuer("auth-service")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateServiceAccessToken(
            String serviceName,
            Long actingForUserId,
            List<String> audiences,
            List<String> scopes,
            Instant issuedAt,
            Instant expiresAt
    ) {
        var builder = Jwts.builder()
                .subject(serviceName)
                .claim("token_type", "service")
                .claim("service_name", serviceName)
                .claim("scope", String.join(" ", scopes))
                .claim("aud", audiences)
                .issuer("auth-service")
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt));
        if (actingForUserId != null) {
            builder.claim("acting_for_user_id", actingForUserId);
        }
        return builder.signWith(privateKey, SignatureAlgorithm.RS256).compact();
    }
}
