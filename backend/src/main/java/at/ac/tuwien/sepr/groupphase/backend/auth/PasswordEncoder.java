package at.ac.tuwien.sepr.groupphase.backend.auth;

import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;

/**
 * Utility class to provide password encoding.
 *
 * @author Marc Putz
 */
public class PasswordEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private PasswordEncoder() {}

    /**
     * Encodes a password for use in data store.
     * Uses email as salt and SHA-384 as hash function.
     *
     * @author Marc Putz
     * @param password the password to encode
     * @param email the corresponding user's email (used as salt)
     * @return the encoded password
     */
    public static String encode(String password, String email) {
        LOGGER.trace("encode({},{})", password, email);

        // add email as salt to the password
        String saltedPassword = password + email;
        // hash the password
        return Hashing.sha384().hashString(saltedPassword, StandardCharsets.UTF_8).toString();
    }
}
