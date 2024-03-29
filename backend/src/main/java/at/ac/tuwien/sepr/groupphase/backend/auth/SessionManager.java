package at.ac.tuwien.sepr.groupphase.backend.auth;

import at.ac.tuwien.sepr.groupphase.backend.utils.AuthTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to handle user sessions and manage authentication tokens.
 *
 * @author Marc Putz
 */
public final class SessionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static SessionManager INSTANCE = null;

    /**
     * Gets the simpleton SessionManager instance.
     *
     * @return the instance of SessionManager.
     * @author Marc Putz
     */
    public static SessionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager();
        }
        return INSTANCE;
    }

    private final ConcurrentHashMap<String, Long> activeAuthentications = new ConcurrentHashMap<>();
    private final List<Long> activeUsers = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentHashMap<Date, List<String>> tokenExpirationTimes = new ConcurrentHashMap<>();

    private SessionManager() {
        SessionCleaner cleanerThread = new SessionCleaner(this);
        cleanerThread.start();
    }

    /**
     * Registers a new user session.
     * Maps an authentication token to it's corresponding user.
     *
     * @param userId    the ID of the user to create a new session for.
     * @param authToken an authentication token to map to the specified user.
     * @return {@code true} if session was created successfully, {@code false} if something went wrong
     * @author Marc Putz
     */
    public boolean startUserSession(long userId, String authToken) {
        LOGGER.trace("startUserSession({},{})", userId, authToken);

        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalArgumentException("AuthToken is NULL or empty string");
        }

        // check if user already has session
        if (activeUsers.contains(userId)) {
            String oldToken = getAuthTokenForUser(userId);
            if (oldToken != null && !this.stopUserSession(oldToken)) {
                // could not stop old user session
                return false;
            }
        }

        Date expirationDate = AuthTokenUtils.getExpirationDate(authToken);
        if (expirationDate != null) {
            if (tokenExpirationTimes.containsKey(expirationDate)) {
                List<String> tokens = Collections.synchronizedList(new ArrayList<>());
                for (String t : tokenExpirationTimes.get(expirationDate)) {
                    tokens.add(authToken);
                }
                tokenExpirationTimes.put(expirationDate, tokens);
            } else {
                tokenExpirationTimes.put(expirationDate, Collections.synchronizedList(List.of(authToken)));
            }
        }

        activeUsers.add(userId);
        activeAuthentications.put(authToken, userId);

        return true;
    }

    /**
     * Retrives the corresponding user of an authentication token.
     *
     * @param authToken the authentication token of the user
     * @return the ID of the corresponding user, NULL if no session registered
     * @author Marc Putz
     */
    public Long getUserFromAuthToken(String authToken) {
        LOGGER.trace("getUserFromAuthToken({})", authToken);

        return activeAuthentications.getOrDefault(authToken, null);
    }

    /**
     * Retrieves the corresponding authentication token of a user.
     *
     * @param userId the ID of the user
     * @return the corresponding authentication token of the user, NULL if no session registered
     * @author Marc Putz
     */
    public String getAuthTokenForUser(long userId) {
        LOGGER.trace("getAuthTokenForUser({})", userId);

        for (Map.Entry<String, Long> entry : activeAuthentications.entrySet()) {
            if (entry.getValue() == userId) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Stops a user session. Removes mapped user ID of the specified authentication token
     *
     * @param authToken the authentication token of the session to stop.
     * @return {@code true} if session was stopped successfully, {@code false} if something went wrong
     * @author Marc Putz
     */
    public boolean stopUserSession(String authToken) {
        LOGGER.trace("stopUserSession({})", authToken);

        Date expirationDate = AuthTokenUtils.getExpirationDate(authToken);
        if (expirationDate != null && tokenExpirationTimes.containsKey(expirationDate)) {
            List<String> currentTokens = tokenExpirationTimes.get(expirationDate);
            List<String> newTokens = new ArrayList<>();
            for (String curr : currentTokens) {
                if (!curr.equals(authToken)) {
                    newTokens.add(curr);
                }
            }
            tokenExpirationTimes.put(expirationDate, newTokens);
        }

        Long removedUserId = activeAuthentications.remove(authToken);
        if (removedUserId == null) {
            // session did not exist
            return false;
        }

        activeUsers.remove(removedUserId);

        return true;
    }

    /**
     * Cleaner thread for SessionManager.
     * Periodically removes all expired sessions (in interval of {@value SessionCleaner#CLEANUP_INTERVAL_IN_MINUTES} minutes).
     *
     * @author Marc Putz
     */
    private static class SessionCleaner extends Thread {

        private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

        private static final long CLEANUP_INTERVAL_IN_MINUTES = 10;

        private final SessionManager manager;
        private LocalDateTime lastChecked;

        public SessionCleaner(SessionManager manager) {
            this.manager = manager;
            this.lastChecked = LocalDateTime.now();
        }

        @Override
        public void run() {
            LOGGER.debug("UserSession Cleanup Thread has started");

            while (!this.isInterrupted()) {
                try {
                    LocalDateTime nextCheckTime = lastChecked.plusMinutes(CLEANUP_INTERVAL_IN_MINUTES);
                    if (LocalDateTime.now().isEqual(nextCheckTime) || LocalDateTime.now().isAfter(nextCheckTime)) {


                        // check token expiration dates
                        List<String> expiredTokens = Collections.synchronizedList(new ArrayList<>());
                        manager.tokenExpirationTimes.forEachEntry(10L, entry -> {
                            if (entry.getKey().before(new Date())) {
                                // tokens are expired
                                expiredTokens.addAll(entry.getValue());
                            }
                        });

                        // stop sessions of expired tokens
                        expiredTokens.forEach(token -> {
                            if (!manager.stopUserSession(token)) {
                                LOGGER.warn("Unable to stop expired user session '" + token + "'");
                            }
                        });

                        lastChecked = LocalDateTime.now();

                    }
                    //noinspection BusyWait
                    Thread.sleep(CLEANUP_INTERVAL_IN_MINUTES * 60 * 1000);
                } catch (InterruptedException e) {
                    this.interrupt();
                }
            }

            LOGGER.debug("UserSession Cleanup Thread was interrupted");
        }
    }
}
