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

public class AuthTokenUtils {

    private static final String TOKEN_PREFIX = "Token ";
    private static final int TOKEN_VALID_DURATION_IN_MINUTES = 15;
    private static final byte[] TOKEN_SECRET = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();

    private static KeyService keyService = new FileKeyService();

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

    public static Date getExpirationDate(String authToken) {
        return parseToken(authToken).getPayload().getExpiration();
    }

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
