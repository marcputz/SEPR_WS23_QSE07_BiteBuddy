package at.ac.tuwien.sepr.groupphase.backend.auth;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.io.UnsupportedEncodingException;
import java.util.Date;

public class AuthToken {

    private static final String TOKEN_PREFIX = "Token ";
    private static final int TOKEN_VALID_DURATION_IN_MINUTES = 15;

    private static byte[] TOKEN_SECRET = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();

    public static String createToken(long userId, String nickname) {

        Date now = new Date();
        Date expiration = new Date();
        expiration.setTime(now.getTime() + (TOKEN_VALID_DURATION_IN_MINUTES * 60 * 1000));

        String jwt = Jwts.builder()
            .signWith(Keys.hmacShaKeyFor(TOKEN_SECRET), SignatureAlgorithm.HS512)
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

}
