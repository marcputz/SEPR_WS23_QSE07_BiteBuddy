package at.ac.tuwien.sepr.groupphase.backend.auth;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class handles and saves all current user sessions.
 */
public class SessionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private SessionManager() {}

}
