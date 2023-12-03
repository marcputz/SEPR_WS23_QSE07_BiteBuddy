package at.ac.tuwien.sepr.groupphase.backend.auth;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public class PasswordEncoder {
    private PasswordEncoder() {}

    public static String encode(String password, String email) {
        // add email as salt to the password
        String saltedPassword = password + email;
        // hash the password
        return Hashing.sha384().hashString(saltedPassword, StandardCharsets.UTF_8).toString();
    }
}
