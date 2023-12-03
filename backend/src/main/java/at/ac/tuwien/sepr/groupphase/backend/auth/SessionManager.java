package at.ac.tuwien.sepr.groupphase.backend.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class handles and saves all current user sessions.
 */
public class SessionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final ConcurrentHashMap<String, Long> activeAuthentications = new ConcurrentHashMap<>();
    private static final List<Long> activeUsers = Collections.synchronizedList(new ArrayList<>());
    private static final ConcurrentHashMap<Date, List<String>> tokenExpirationTimes = new ConcurrentHashMap<>();

    private SessionManager() {}

    public static boolean startUserSession(long userId, String authToken) {
        // check if user already has session
        if (activeUsers.contains(userId)) {
            return false;
        }

        Date expirationDate = AuthTokenUtils.getExpirationDate(authToken);
        if (tokenExpirationTimes.containsKey(expirationDate)) {
            List<String> tokens = tokenExpirationTimes.get(expirationDate);
            tokens.add(authToken);
            tokenExpirationTimes.put(expirationDate, tokens);
        } else {
            tokenExpirationTimes.put(expirationDate, List.of(authToken));
        }

        activeUsers.add(userId);
        activeAuthentications.put(authToken, userId);

        return true;
    }

    public static Long getUserFromAuthToken(String authToken) {
        return activeAuthentications.getOrDefault(authToken, null);
    }

    public static boolean stopUserSession(String authToken) {
        Date expirationDate = AuthTokenUtils.getExpirationDate(authToken);
        if (tokenExpirationTimes.containsKey(expirationDate)) {
            List<String> tokens = tokenExpirationTimes.get(expirationDate);
            tokens.remove(authToken);
            tokenExpirationTimes.put(expirationDate, tokens);
        }

        Long removedUserId = activeAuthentications.remove(authToken);
        if (removedUserId == null) {
            // session did not exist
            return false;
        }

        activeUsers.remove(removedUserId);

        return true;
    }
}
