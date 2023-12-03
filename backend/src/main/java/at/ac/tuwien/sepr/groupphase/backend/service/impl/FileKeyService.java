package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.service.KeyService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class FileKeyService implements KeyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String DEFAULT_KEY_FOLDER = (new File("")).getAbsolutePath() + "/src/main/resources/security";

    private static final String PRIVATE_KEY_FILENAME = "private.pem";
    private static final String PUBLIC_KEY_FILENAME = "public.pem";

    private File keyFolder = null;

    public FileKeyService() {
        this(new File(DEFAULT_KEY_FOLDER));
    }

    public FileKeyService(File keyFolder) {
        LOGGER.debug("Using key folder at '" + keyFolder.getAbsolutePath() + "'");

        if (keyFolder == null || !keyFolder.exists()) {
            throw new IllegalArgumentException("Key folder does not exist or is NULL value");
        }

        if (!keyFolder.canRead()) {
            throw new IllegalArgumentException("Application has no permission to read key folder at '" + keyFolder.getAbsolutePath() + "'");
        }

        if (!keyFolder.isDirectory()) {
            throw new IllegalArgumentException("Specified key folder is not a directory");
        }

        this.keyFolder = keyFolder;
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        LOGGER.trace("getPrivateKey()");

        File file = new File(this.keyFolder.getAbsolutePath() + "/" + PRIVATE_KEY_FILENAME);

        if (file == null || !file.exists()) {
            LOGGER.error("private key file does not exist");
            return null;
        }

        if (!file.canRead()) {
            LOGGER.error("Application has no read permission on private key file");
            return null;
        }

        if (!file.isFile()) {
            LOGGER.error("Specified private key filepath is not a file");
            return null;
        }

        try {

            // read file content
            String key = Files.readString(file.toPath(), Charset.defaultCharset());

            // remove headers
            String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

            byte[] encoded = Base64.decodeBase64(privateKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);

            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

        } catch (IOException ex) {
            LOGGER.error("Could not read private key file: " + ex.getMessage());
            return null;
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error("Application environment is unable to read RSA keys");
            return null;
        } catch (InvalidKeySpecException ex) {
            LOGGER.error("Private key is not in valid format");
            return null;
        }
    }

    @Override
    public RSAPublicKey getPublicKey() {
        LOGGER.trace("getPublicKey()");

        File file = new File(this.keyFolder.getAbsolutePath() + "/" + PUBLIC_KEY_FILENAME);

        if (file == null || !file.exists()) {
            LOGGER.error("Public key file does not exist");
            return null;
        }

        if (!file.canRead()) {
            LOGGER.error("Application has no read permission on public key file");
            return null;
        }

        if (!file.isFile()) {
            LOGGER.error("Specified public key filepath is not a file");
            return null;
        }

        try {

            // read file content
            String key = Files.readString(file.toPath(), Charset.defaultCharset());

            // remove headers
            String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");

            byte[] encoded = Base64.decodeBase64(publicKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);

            return (RSAPublicKey) keyFactory.generatePublic(keySpec);

        } catch (IOException ex) {
            LOGGER.error("Could not read public key file: " + ex.getMessage());
            return null;
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error("Application environment is unable to read RSA keys");
            return null;
        } catch (InvalidKeySpecException ex) {
            LOGGER.error("Public key is not in valid format");
            return null;
        }
    }
}
