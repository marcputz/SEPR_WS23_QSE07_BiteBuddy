package at.ac.tuwien.sepr.groupphase.backend.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This class handles and saves all current user sessions.
 */
public final class SessionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static SessionManager INSTANCE = null;

    public static SessionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager();
        }
        return INSTANCE;
    }

    private final SessionCleaner cleanerThread;

    private final ConcurrentHashMap<String, Long> activeAuthentications = new ConcurrentHashMap<>();
    private final List<Long> activeUsers = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentHashMap<Date, List<String>> tokenExpirationTimes = new ConcurrentHashMap<>();

    private SessionManager() {
        cleanerThread = new SessionCleaner(this);
        cleanerThread.start();
    }

    public boolean startUserSession(long userId, String authToken) {
        // check if user already has session
        if (activeUsers.contains(userId)) {
            String oldToken = getAuthTokenForUser(userId);
            if (!this.stopUserSession(oldToken)) {
                // could not stop old user session
                return false;
            }
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

    public Long getUserFromAuthToken(String authToken) {
        return activeAuthentications.getOrDefault(authToken, null);
    }

    public String getAuthTokenForUser(long userId) {
        for (Map.Entry<String, Long> entry : activeAuthentications.entrySet()) {
            if (entry.getValue() == userId) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean stopUserSession(String authToken) {
        Date expirationDate = AuthTokenUtils.getExpirationDate(authToken);
        if (tokenExpirationTimes.containsKey(expirationDate)) {
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

    private static class SessionCleaner extends Thread {

        private static final long CLEANUP_INTERVAL_IN_MINUTES = 10;

        private final SessionManager manager;
        private LocalDateTime lastChecked;

        public SessionCleaner(SessionManager manager) {
            this.manager = manager;
            this.lastChecked = LocalDateTime.now();
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                try {
                    LocalDateTime nextCheckTime = lastChecked.plusMinutes(CLEANUP_INTERVAL_IN_MINUTES);
                    if (LocalDateTime.now().isEqual(nextCheckTime) || LocalDateTime.now().isAfter(nextCheckTime)) {
                        LOGGER.trace("Cleaning up expired user sessions");

                        // check token expiration dates
                        List<String> expiredTokens = Collections.synchronizedList(new ArrayList<>());
                        manager.tokenExpirationTimes.forEachEntry(10L, entry -> {
                            if (entry.getKey().before(new Date())) {
                                // tokens are expired
                                expiredTokens.addAll(entry.getValue());
                            }
                        });

                        if (expiredTokens.size() > 0) {
                            LOGGER.debug("Timeout for " + expiredTokens.size() + " token(s)");
                        }

                        // stop sessions of expired tokens
                        expiredTokens.forEach(token -> {
                            LOGGER.trace("Stopping session '" + token + "'");
                            if (!manager.stopUserSession(token)) {
                                LOGGER.warn("Unable to stop session '" + token + "'");
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
        }
    }
}
