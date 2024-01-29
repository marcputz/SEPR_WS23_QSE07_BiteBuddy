package at.ac.tuwien.sepr.groupphase.backend.service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Service interface for asymmetric RSA keys.
 *
 * @author Marc Putz
 */
public interface KeyService {

    /**
     * Retrieves the application's private key (warning: never share or transmit publicly).
     *
     * @author Marc Putz
     * @return application's RSA private key.
     */
    RSAPrivateKey getPrivateKey();

    /**
     * Retrieves the application's public key.
     *
     * @author Marc Putz
     * @return application's RSA public key.
     */
    RSAPublicKey getPublicKey();

}
