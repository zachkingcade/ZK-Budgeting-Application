package zachkingcade.dev.ledger.adapter.in.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
public class JwtDecoderConfig {

    private static final String EXPECTED_ISSUER = "auth-service";
    private static final String EXPECTED_AUDIENCE = "ledger-service";

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        String pem;
        try (InputStream is = new ClassPathResource("keys/public_key.pem").getInputStream()) {
            pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        String normalized = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(normalized);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(EXPECTED_ISSUER);
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(EXPECTED_AUDIENCE);
        OAuth2TokenValidator<Jwt> tokenTypeValidator = new TokenTypeValidator();

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                withIssuer,
                audienceValidator,
                tokenTypeValidator
        );

        decoder.setJwtValidator(validator);
        return decoder;
    }

    static class AudienceValidator implements OAuth2TokenValidator<Jwt> {

        private final String expectedAudience;

        AudienceValidator(String expectedAudience) {
            this.expectedAudience = expectedAudience;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            List<String> audience = jwt.getAudience();

            if (audience != null && audience.contains(expectedAudience)) {
                return OAuth2TokenValidatorResult.success();
            }

            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "The required audience is missing",
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        }
    }

    static class TokenTypeValidator implements OAuth2TokenValidator<Jwt> {

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            if (!jwt.hasClaim("token_type")) {
                return OAuth2TokenValidatorResult.success();
            }
            String tokenType = jwt.getClaimAsString("token_type");
            if ("user".equals(tokenType) || "service".equals(tokenType)) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "Invalid token_type claim",
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        }
    }
}