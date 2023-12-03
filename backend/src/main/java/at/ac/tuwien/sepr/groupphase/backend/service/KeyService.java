package at.ac.tuwien.sepr.groupphase.backend.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public interface KeyService {

    public RSAPrivateKey getPrivateKey();

    public RSAPublicKey getPublicKey();

}
