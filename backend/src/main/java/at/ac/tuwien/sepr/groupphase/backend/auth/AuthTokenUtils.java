package at.ac.tuwien.sepr.groupphase.backend.auth;


import at.ac.tuwien.sepr.groupphase.backend.service.KeyService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.FileKeyService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

/**
 * Utility class for JWT authentication tokens
 *
 * @author Marc Putz
 */
public class AuthTokenUtils {

    private static final String TOKEN_PREFIX = "Token ";
    private static final int TOKEN_VALID_DURATION_IN_MINUTES = 120;
    private static final byte[] TOKEN_SECRET = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();

    private static KeyService keyService = new FileKeyService();
    /**
     * Creates a new JWT authentication token, assigned to a specific user identified by ID and nickname.
     * The JWT token is signed with RSA keys, meaning it is signed with the server's private key and can
     * later be verified by using the server's public key.
     *
     * The JWT uses a time interval of {@value AuthTokenUtils#TOKEN_VALID_DURATION_IN_MINUTES} minutes to calculate the expiration date.
     *
     * Tokens are also automatically prefixed by a string specified in {@link AuthTokenUtils#TOKEN_PREFIX}.
     *
     * @author Marc Putz
     * @param user the ApplicationUser object of the correlating user. Uses ID and nickname for token creation.
     * @return the JWT authentication token as a string.
     */

    /**
     * Creates a new JWT authentication token, assigned to a specific user identified by ID and nickname.
     * The JWT token is signed with RSA keys, meaning it is signed with the server's private key and can
     * later be verified by using the server's public key.
     *
     * The JWT uses a time interval of {@value AuthTokenUtils#TOKEN_VALID_DURATION_IN_MINUTES} minutes to calculate the expiration date.
     *
     * Tokens are also automatically prefixed by a string specified in {@link AuthTokenUtils#TOKEN_PREFIX}.
     *
     * @author Marc Putz
     * @param userId the User ID of the correlating user
     * @param nickname the nickname of the correlating user
     * @return the JWT authentication token as a string.
     */
    public static String createToken(long userId, String nickname) {

        RSAPrivateKey privateKey = keyService.getPrivateKey();

        Date now = new Date();
        Date expiration = new Date();
        expiration.setTime(now.getTime() + (TOKEN_VALID_DURATION_IN_MINUTES * 60 * 1000));

        String jwt = Jwts.builder()
            .signWith(privateKey)
            .header()
            .type("JWT")
            .and()
            .issuer("BiteBuddy-Server") // issuer of the token, aka. this server
            .subject(nickname) // to whom this token relates to, aka. the user
            .setAudience("BiteBuddy-App")
            .expiration(expiration)
            .issuedAt(now)
            .id(Long.valueOf(userId).toString())
            .compact();

        return TOKEN_PREFIX + jwt;
    }

    /**
     * Verifies if a token contains valid data and was signed by this server.
     *
     * @param authToken the JWT authentication token to verify
     * @return {@code true}, if token valid. {@code false}, if token data, format or signature invalid
     */
    public static boolean isValid(String authToken) {
        Claims claims = parseToken(authToken).getPayload();

        // validate parsed token
        if (claims.getAudience().contains("BiteBuddy-App")) {
            if (claims.getIssuer().equals("BiteBuddy-Server")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the expiration date of a JWT authentication token.
     *
     * @param authToken a JWT token
     * @return the token's expiration date. NULL if token invalid or has no expiration date
     */
    public static Date getExpirationDate(String authToken) {
        return parseToken(authToken).getPayload().getExpiration();
    }

    /**
     * Parses a (signed) JWT string to the corresponding signed JWT (called JWS) class.
     * Does not accept non-signed JWTs or encrypted JWTs (called JWEs).
     *
     * @param authToken a signed JWT token in string format.
     * @return the parsed JWS object.
     * @throws io.jsonwebtoken.JwtException if the JWT string cannot be parsed or validated
     * @throws IllegalArgumentException if authToken is NULL, empty or only whitespace
     */
    private static Jws<Claims> parseToken(String authToken) {

        // Remove prefix
        authToken = authToken.substring(TOKEN_PREFIX.length());

        RSAPublicKey publicKey = keyService.getPublicKey();

        JwtParser parser = Jwts.parser()
            .verifyWith(publicKey)
            .build();

        return parser.parseSignedClaims(authToken);
    }

}
