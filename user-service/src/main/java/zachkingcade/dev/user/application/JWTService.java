package zachkingcade.dev.user.application;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        String pem = Files.readString(Path.of("src/main/resources/keys/private_key.pem"));
        String privateKeyString = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        System.out.println("NORMALIZED LENGTH: " + privateKeyString.length());
        System.out.println("NORMALIZED END: " + privateKeyString.substring(privateKeyString.length() - 20));

        // Decode Base64 string to byte array
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);

        // Create a key
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        // Create privateKey object
        KeyFactory kf = KeyFactory.getInstance("RSA");
        privateKey = kf.generatePrivate(spec);
    }

    public String generateAccessToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(10 * 60);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuer("auth-service")
                .setAudience("ledger-service")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
